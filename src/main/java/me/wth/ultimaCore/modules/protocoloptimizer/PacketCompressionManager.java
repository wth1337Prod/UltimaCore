package me.wth.ultimaCore.modules.protocoloptimizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class PacketCompressionManager {
    
    private final ProtocolOptimizerModule module;
    private final ConcurrentHashMap<String, byte[]> compressionCache;
    private int compressionLevel;
    
    
    public PacketCompressionManager(ProtocolOptimizerModule module) {
        this.module = module;
        this.compressionCache = new ConcurrentHashMap<>();
        this.compressionLevel = module.getCompressionLevel();
    }
    
    
    public Object compressPacket(String packetType, Object packetData) {
        if (packetData == null) return null;
        
                String cacheKey = packetType + "_" + packetData.hashCode();
        if (compressionCache.containsKey(cacheKey)) {
            return compressionCache.get(cacheKey);
        }
        
                byte[] input;
        if (packetData instanceof byte[]) {
            input = (byte[]) packetData;
        } else {
            input = packetData.toString().getBytes();
        }
        
                byte[] compressed = compress(input);
        
                if (compressed != null && compressed.length < input.length) {
            compressionCache.put(cacheKey, compressed);
            return compressed;
        }
        
                return packetData;
    }
    
    
    private byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(compressionLevel);
        deflater.setInput(data);
        deflater.finish();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        
        try {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException e) {
            return data;
        } finally {
            deflater.end();
        }
        
        return outputStream.toByteArray();
    }
    
    
    public byte[] decompress(byte[] compressed) {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressed.length);
        byte[] buffer = new byte[1024];
        
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (Exception e) {
            return compressed;
        } finally {
            inflater.end();
        }
        
        return outputStream.toByteArray();
    }
    
    
    public void setCompressionLevel(int level) {
        if (level >= 0 && level <= 9) {
            this.compressionLevel = level;
        }
    }
    
    
    public void clearCache() {
        compressionCache.clear();
    }
} 