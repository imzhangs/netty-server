package com.xxx.netty.common.redis;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisInitBean {
    
    private List<String> Host;
    private long maxWaitMillis;
    private int MaxIdle;
    private Boolean testOnBorrow;
    private static List<JedisShardInfo> shards ;
    private static ShardedJedisPool pool;
    private static ShardedJedis jedis;
    
    
    
    /**
     * redis初始
     * @param host
     * @param maxWaitMillis
     * @param maxIdle 
     * @param testOnBorrow
     */
    public RedisInitBean(List<String> host, long maxWaitMillis, int maxIdle,Boolean testOnBorrow) {
        super();
        Logger log = Logger.getLogger(getClass().getName());
        Host = host;
        this.maxWaitMillis = maxWaitMillis;
        MaxIdle = maxIdle;
        this.testOnBorrow = testOnBorrow;
        if(host!=null && host.size()!=0){
            for (int i = 0; i < host.size(); i++) {        
                String h[] = ((String) host.get(i)).split(":");     
                shards = Arrays.asList(new JedisShardInfo(h[0].trim(),Integer.parseInt(h[1].trim())));
                log.info("redis-domain:"+shards.toString());
            }
        }else{
        	log.info("========请检查Redis配置，host项为必填项！格式[IP:PORT]");
        }

        pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
        jedis = pool.getResource();    
    }
    
    public synchronized ShardedJedis getSingletonInstance(){
        return jedis;
    }
    
    @SuppressWarnings("deprecation")
	public synchronized static void returnResource(){
        pool.returnResource(jedis);
    }
    
    public synchronized static void destroy(){
        pool.destroy();
    }

	public List<String> getHost() {
		return Host;
	}

	public void setHost(List<String> host) {
		Host = host;
	}

	public long getMaxWaitMillis() {
		return maxWaitMillis;
	}

	public void setMaxWaitMillis(long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	public int getMaxIdle() {
		return MaxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		MaxIdle = maxIdle;
	}

	public Boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}
    
    
}