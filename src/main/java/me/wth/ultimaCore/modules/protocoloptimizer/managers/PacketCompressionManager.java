package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


public class PacketCompressionManager {
    private final ProtocolOptimizerModule module;
    
        private final Map<String, byte[]> compressionCache = new HashMap<>();
    
        private long totalOriginalSize = 0;
    private long totalCompressedSize = 0;
    private int totalPacketsCompressed = 0;
    
    
    public PacketCompressionManager(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    
    public Object compressPacket(String packetType, Object packetData) {
        if (packetData == null) return null;
        
        try {
                        byte[] serialized = serializePacket(packetData);
            
            if (serialized == null || serialized.length <= 64) {
                                return packetData;
            }
            
                        String cacheKey = packetType + "_" + serialized.length;
            if (compressionCache.containsKey(cacheKey)) {
                byte[] cachedData = compressionCache.get(cacheKey);
                if (isPacketDataEqual(serialized, cachedData)) {
                                        return packetData;
                }
            }
            
                        byte[] compressed = compressData(serialized);
            
            if (compressed != null && compressed.length < serialized.length) {
                                int saved = serialized.length - compressed.length;
                totalOriginalSize += serialized.length;
                totalCompressedSize += compressed.length;
                totalPacketsCompressed++;
                
                if (module != null) {
                    module.incrementTrafficSaved(saved);
                    module.incrementPacketsOptimized(1);
                }
                
                                if (serialized.length < 8192) {
                    compressionCache.put(cacheKey, serialized);
                                        if (compressionCache.size() > 1000) {
                        cleanupCache();
                    }
                }
                
                                                Object decompressed = deserializePacket(compressed);
                return decompressed != null ? decompressed : packetData;
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при сжатии пакета " + packetType + ": " + e.getMessage());
        }
        
        return packetData;
    }
    
    
    private byte[] serializePacket(Object packetData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            
            oos.writeObject(packetData);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
    
    
    private Object deserializePacket(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
    
    
    private byte[] compressData(byte[] data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int compressionLevel = module != null ? module.getCompressionLevel() : 6;
            Deflater deflater = new Deflater(compressionLevel);
            try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
                dos.write(data);
                dos.finish();
                return baos.toByteArray();
            }
        } catch (IOException e) {
            return null;
        }
    }
    
    
    private byte[] decompressData(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             InflaterInputStream iis = new InflaterInputStream(bais, new Inflater())) {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = iis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
    
    
    private boolean isPacketDataEqual(byte[] data1, byte[] data2) {
        if (data1.length != data2.length) {
            return false;
        }
        
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    
    private void cleanupCache() {
                compressionCache.clear();
    }
    
    
    public double getSavedPercentage() {
        if (totalOriginalSize == 0) {
            return 0.0;
        }
        
        return 100.0 * (totalOriginalSize - totalCompressedSize) / totalOriginalSize;
    }
    
    
    public long getTotalSaved() {
        return totalOriginalSize - totalCompressedSize;
    }
    
    
    public int getTotalPacketsCompressed() {
        return totalPacketsCompressed;
    }
} 