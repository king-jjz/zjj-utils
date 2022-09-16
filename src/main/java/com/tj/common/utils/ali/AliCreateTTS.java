package com.tj.common.utils.ali;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.tj.common.utils.uuid.UUIDUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;

public class AliCreateTTS {




    private static final String appkey = "";

    public static String getWav(String voice,String volume,String speechRate,String pitchRate,String text) throws ClientException {
        //人物 //音量 //语速 //语调 //词
        // 采用RFC 3986规范进行urlencode编码。
        String textUrlEncode = text;
        try {
            textUrlEncode = URLEncoder.encode(textUrlEncode, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(textUrlEncode);
        String fileName = UUIDUtils.getUUID()+ ".wav";
        String audioSaveFile = "wav/tts/"+fileName;
        String format = "wav";
        int sampleRate = 16000;
        processGETRequet(textUrlEncode, audioSaveFile, format, sampleRate, voice,volume,speechRate,pitchRate);
        return "https:///tts/"+fileName;
    }

    /**
     * HTTPS GET请求
     */
    public static void processGETRequet(String text, String audioSaveFile, String format, int sampleRate, String voice,String volume,String speechRate,String pitchRate) throws ClientException {
        /**
         * 设置HTTPS GET请求：
         * 1.使用HTTPS协议
         * 2.语音识别服务域名：nls-gateway-cn-shanghai.aliyuncs.com
         * 3.语音识别接口请求路径：/stream/v1/tts
         * 4.设置必须请求参数：appkey、token、text、format、sample_rate
         * 5.设置可选请求参数：voice、volume、speech_rate、pitch_rate
         */
        String url = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/tts";
        url = url + "?appkey=" + appkey;
        url = url + "&token=" + AliYun.getTocken();
        url = url + "&text=" + text;
        url = url + "&format=" + format;
        //url = url + "&voice=" + voice;
        url = url + "&sample_rate=" + String.valueOf(sampleRate);
        // voice 发音人，可选，默认是xiaoyun。
        url = url + "&voice=" + voice;
        // volume 音量，范围是0~100，可选，默认50。
        url = url + "&volume=" + volume;
        // speech_rate 语速，范围是-500~500，可选，默认是0。
        url = url + "&speech_rate=" + speechRate;
        // pitch_rate 语调，范围是-500~500，可选，默认是0。
        url = url + "&pitch_rate=" + pitchRate;
        System.out.println("URL: " + url);
        /**
         * 发送HTTPS GET请求，处理服务端的响应。
         */
        Request request = new Request.Builder().url(url).get().build();
        try {
            long start = System.currentTimeMillis();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            System.out.println("total latency :" + (System.currentTimeMillis() - start) + " ms");
            System.out.println(response.headers().toString());
            String contentType = response.header("Content-Type");
            if ("audio/mpeg".equals(contentType)) {
                File f = new File(audioSaveFile);
                FileOutputStream fout = new FileOutputStream(f);
                fout.write(response.body().bytes());
                fout.close();
                System.out.println("The GET request succeed!");
            }
            else {
                // ContentType 为 null 或者为 "application/json"
                String errorMessage = response.body().string();
                System.out.println("The GET request failed: " + errorMessage);
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * HTTPS POST请求
     */
    public void processPOSTRequest(String text, String audioSaveFile, String format, int sampleRate, String voice) throws ClientException {
        /**
         * 设置HTTPS POST请求：
         * 1.使用HTTPS协议
         * 2.语音合成服务域名：nls-gateway-cn-shanghai.aliyuncs.com
         * 3.语音合成接口请求路径：/stream/v1/tts
         * 4.设置必须请求参数：appkey、token、text、format、sample_rate
         * 5.设置可选请求参数：voice、volume、speech_rate、pitch_rate
         */
        String url = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/tts";
        JSONObject taskObject = new JSONObject();
        taskObject.put("appkey", appkey);
        taskObject.put("token", AliYun.getTocken());
        taskObject.put("text", text);
        taskObject.put("format", format);
        taskObject.put("voice", voice);
        taskObject.put("sample_rate", sampleRate);
        // voice 发音人，可选，默认是xiaoyun。
        // taskObject.put("voice", "xiaoyun");
        // volume 音量，范围是0~100，可选，默认50。
        // taskObject.put("volume", 50);
        // speech_rate 语速，范围是-500~500，可选，默认是0。
        // taskObject.put("speech_rate", 0);
        // pitch_rate 语调，范围是-500~500，可选，默认是0。
        // taskObject.put("pitch_rate", 0);
        String bodyContent = taskObject.toJSONString();
        System.out.println("POST Body Content: " + bodyContent);
        RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), bodyContent);
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(reqBody)
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String contentType = response.header("Content-Type");
            if ("audio/mpeg".equals(contentType)) {
                File f = new File(audioSaveFile);
                FileOutputStream fout = new FileOutputStream(f);
                fout.write(response.body().bytes());
                fout.close();
                System.out.println("The POST request succeed!");
            }
            else {
                // ContentType 为 null 或者为 "application/json"
                String errorMessage = response.body().string();
                System.out.println("The POST request failed: " + errorMessage);
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws ClientException {

        String text = "今天是周一，天气挺好的。";
        // 采用RFC 3986规范进行urlencode编码。
        String textUrlEncode = text;
        try {
            textUrlEncode = URLEncoder.encode(textUrlEncode, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(textUrlEncode);
        String audioSaveFile = UUIDUtils.getUUID()+ ".wav";
        String format = "wav";
        int sampleRate = 16000;
        //processGETRequet(textUrlEncode, audioSaveFile, format, sampleRate, "siyue");
        //processPOSTRequest(text, audioSaveFile, format, sampleRate, "siyue");
        System.out.println("### Game Over ###");
    }
}
