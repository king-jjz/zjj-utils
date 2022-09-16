package com.tj.common.utils.mail;

import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

public class SendEmailUtils {




    /**
     * 发送邮件
     * @param email  需要发送的邮件地址，多个邮箱使用 , 分隔
     * @param content 发送的内容
     */
    public static void send(String email,String content,String protocol,String host,String port,String account,String password,String sender) throws Exception {
        Session session = initProperties( protocol, host, port, account, password);
        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(account,sender));
        //收件人,多人接收
        InternetAddress[] internetAddressesTo = new InternetAddress().parse(email);
        mimeMessage.setRecipients(Message.RecipientType.TO, internetAddressesTo);
        //主题
        mimeMessage.setSubject("Intenginetech Email");
        //时间
        mimeMessage.setSentDate(new Date());
        //容器类  附件
        MimeMultipart mimeMultipart = new MimeMultipart();
        //可以包装文本，图片，附件
        MimeBodyPart bodyPart = new MimeBodyPart();
        //设置内容
        bodyPart.setContent(content,"text/html; charset=UTF-8");
        mimeMultipart.addBodyPart(bodyPart);
        //添加图片&附件
//            bodyPart = new MimeBodyPart();
//            bodyPart.attachFile("附件地址");
        mimeMessage.setContent(mimeMultipart);
        mimeMessage.saveChanges();
        //发送
        Transport.send(mimeMessage);
    }

    public static Session initProperties(String protocol,String host,String port,String account,String password){
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol",protocol);
        properties.setProperty("mail.smtp.host",host);
        properties.setProperty("mail.smtp.port",port);
        //使用smtp身份验证
        properties.setProperty("mail.smtp.auth","true");
        //使用SSL，企业邮箱必须开启  、开启安全协议
        MailSSLSocketFactory mailSSLSocketFactory = null;
        try {
            mailSSLSocketFactory = new MailSSLSocketFactory();
            mailSSLSocketFactory.setTrustAllHosts(true);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory",mailSSLSocketFactory);
        properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback","false");
        properties.put("mail.smtp.socketFactory.port",port);
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account,password);
            }
        });
        session.setDebug(false);
        return session;
    }
}
