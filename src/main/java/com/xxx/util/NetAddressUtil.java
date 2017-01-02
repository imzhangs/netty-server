package com.xxx.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

public class NetAddressUtil {

	public static void main(String[] args) throws Throwable {
		long time=System.currentTimeMillis();
		System.out.println(getIpAddrInfoStr("http://ip.taobao.com/service/getIpInfo2.php?","ip=myip"));
		System.out.println(System.currentTimeMillis()-time+"ms");
		time=System.currentTimeMillis();
		System.out.println(getInetnetIp());
		System.out.println(System.currentTimeMillis()-time+"ms");
	}

	/**
	 * 获取IP归属的信息
	 * 
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static TaobaoIpInfo getIpAddrInfo(String url,String param) throws Exception {
		String result= HttpRequestUtil.request(url, param, "post");
		return JSONObject.parseObject(result, TaobaoIpInfo.class);
	}
	public static String getIpAddrInfoStr(String url,String param) throws Exception {
		String result= HttpRequestUtil.request(url, param, "post");
		TaobaoIpInfo info= JSONObject.parseObject(result, TaobaoIpInfo.class);
		if(null!=info && info.data!=null && info.data.get("ip")!=null){
			return info.data.get("ip").toString();
		}
		return "";
	}

	/**
	 * 从ip138 获得 当前外网IP 
	 * @return
	 * @throws Exception
	 */
	public static String getInetnetIp()throws Exception{
		String url="http://1212.ip138.com/ic.asp";
		String result= HttpRequestUtil.request(url, "", "post");
		if(result.contains("[") && result.contains("]")){
			return result.substring(result.indexOf("[")+1,result.indexOf("]"));
		}
		return "";
	}
	/**
	 * ping包获取域名 外网IP
	 * @throws Exception
	 */
	public static String getIpByPing() throws Exception {
		String []remoteServers={"www.baidu.com","www.taobao.com","www.qq.com","www.163.com"};
		String line = null;
		StringBuilder result=new StringBuilder();
		try {
			Process pro = Runtime.getRuntime().exec("ping " + remoteServers[new Random().nextInt(remoteServers.length)]);
			BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream(),"GBK"));
			while ((line = buf.readLine()) != null)
				result.append(line);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return result.toString();
	}

	/**
	 * 对于linux 适用一般物理环境，多虚拟网卡不适用
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static String getRealIp() throws SocketException {
		String localip = null;// 本地IP，如果没有配置外网IP则返回它
		String netip = null;// 外网IP

		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
		boolean finded = false;// 是否找到外网IP
		while (netInterfaces.hasMoreElements() && !finded) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
					netip = ip.getHostAddress();
					finded = true;
					break;
				} else
					if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
					localip = ip.getHostAddress();
				}
			}
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip;
		}
	}
}

class TaobaoIpInfo{
	public int code;
	public Map<String,Object> data;
}
