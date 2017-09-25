package com.sunny.platform.ris.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PolicyConditions;

public class PolicyUtil {
	private static final String ACCESSID = "accessid";
	private static final String POLICY = "policy";
	private static final String SIGNATURE = "signature";
	private static final String DIR = "dir";
	private static final String HOST = "host";
	private static final String EXPIRE = "expire";
	private static Properties loadConfigProperties() {
		Resource resource = null;
		Properties properties = null;
		try {
			resource = new ClassPathResource("/config/others/config.properties");
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
		
	// 	域名
	private static String getEndPoint() {
		return loadConfigProperties().getProperty("oss.endpoint");
	}
	// accessKeyId	
	private static String getAccessKeyId() {
		return loadConfigProperties().getProperty("oss.accessKeyId");
	}
	// accessKeySecret
	private static String getAccessKeySecret() {
		return loadConfigProperties().getProperty("oss.accessKeySecret");
	}
	// bucketName
	private static String getBucketName() {
		return loadConfigProperties().getProperty("oss.bucketName");
	}
	
	public static Map<String, String> getPolicy(){
		String endpoint = getEndPoint();
        String accessId = getAccessKeyId();
        String accessKey = getAccessKeySecret();
        String bucket = getBucketName();
        String dir = "user-dir";
        String host = "http://" + bucket + "." + endpoint;
        OSSClient client = new OSSClient(endpoint, accessId, accessKey);
        Map<String, String> respMap = null;
		try {
			long expireTime = 10;
        	long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            
            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);
            
            respMap = new LinkedHashMap<String, String>();
            respMap.put(PolicyUtil.ACCESSID, accessId);
            respMap.put(PolicyUtil.POLICY, encodedPolicy);
            respMap.put(PolicyUtil.SIGNATURE, postSignature);
            respMap.put(PolicyUtil.DIR, dir);
            respMap.put(PolicyUtil.HOST, host);
            respMap.put(PolicyUtil.EXPIRE, String.valueOf(expireEndTime / 1000));
            //JSONObject ja1 = JSONObject.fromObject(respMap);
            //System.out.println(ja1.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			client.shutdown();
		}
		return respMap;
	}
	
	// 删除云端文件
	public static void deleteOssByKey(String key){
		String endpoint = getEndPoint();
        String accessId = getAccessKeyId();
        String accessKey = getAccessKeySecret();
        String bucketName = getBucketName();
        OSSClient ossClient = new OSSClient(endpoint, accessId, accessKey);
		try {
			ossClient.deleteObject(bucketName, key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			ossClient.shutdown();
		}
	}
	
	@SuppressWarnings("unused")
	private void getListObject(OSSClient ossClient){
        String bucketName = getBucketName();
    	// 查看Bucket中的Object。详细请参看“SDK手册 > Java-SDK > 管理文件”。
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        try {
        	List<OSSObjectSummary> objectSummary = objectListing.getObjectSummaries();
            System.out.println("您有以下Object：");
            for (OSSObjectSummary object : objectSummary) {
                System.out.println("\t" + object.getKey());
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ossClient.shutdown();
		}
        
    }
	
   @SuppressWarnings("unused")
   private void isExistsBucket(OSSClient ossClient){
	    String bucketName = getBucketName();
	    try {
	    	 if (ossClient.doesBucketExist(bucketName)) {
	             System.out.println("您已经创建Bucket：" + bucketName + "。");
	         } else {
	             System.out.println("您的Bucket不存在");
	             // ossClient.createBucket(bucketName);
	         }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ossClient.shutdown();
		}
       
    }
    
    @SuppressWarnings("unused")
	private  void getBucketInfo(OSSClient ossClient){
    	String bucketName = getBucketName();
    	try {
    		BucketInfo info = ossClient.getBucketInfo(bucketName);
	        System.out.println("Bucket " + bucketName + "的信息如下：");
	        System.out.println("\t数据中心：" + info.getBucket().getLocation());
	        System.out.println("\t创建时间：" + info.getBucket().getCreationDate());
	        System.out.println("\t用户标志：" + info.getBucket().getOwner());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ossClient.shutdown();
		}
        BucketInfo info = ossClient.getBucketInfo(bucketName);
        System.out.println("Bucket " + bucketName + "的信息如下：");
        System.out.println("\t数据中心：" + info.getBucket().getLocation());
        System.out.println("\t创建时间：" + info.getBucket().getCreationDate());
        System.out.println("\t用户标志：" + info.getBucket().getOwner());
    }
    
    @SuppressWarnings("unused")
	private  void putStrObject(OSSClient ossClient,String strkey,String value){
    	// 把字符串存入OSS，Object的名称为firstKey。详细请参看“SDK手册 > Java-SDK > 上传文件”。
    	String bucketName = getBucketName();
    	try {
    		InputStream is = new ByteArrayInputStream(value.getBytes());
    	    ossClient.putObject(bucketName, strkey, is);
    	    System.out.println("Object：" + strkey + "存入OSS成功。");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ossClient.shutdown();
		}
    }
	
    @SuppressWarnings("unused")
	private  void getObjectByStr(OSSClient ossClient,String strkey) throws IOException{
    	// 下载文件。详细请参看“SDK手册 > Java-SDK > 下载文件”。
    	String bucketName = getBucketName();
        OSSObject ossObject = ossClient.getObject(bucketName, strkey);
        try {
        	InputStream inputStream = ossObject.getObjectContent();
            StringBuilder objectContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                objectContent.append(line);
            }
            inputStream.close();
            System.out.println("Object：" + strkey + "的内容是：" + objectContent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ossClient.shutdown();
		}
        
    }
    
    
    
    
}
