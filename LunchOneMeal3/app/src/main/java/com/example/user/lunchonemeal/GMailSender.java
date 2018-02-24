package com.example.user.lunchonemeal;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import static android.R.attr.type;
import static android.R.id.message;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by user on 2017-04-14.
 */

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost="smtp.gmail.com";
    private String user;
    private String password;
    private Session session;
    MimeMessage msg;

    public GMailSender(String user,String password)
    {
        this.user=user;
        this.password=password;

        Properties properties = new Properties();
        properties.put("mail.smtp.user", user); //구글 계정
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        properties.put("mail.smtp.debug", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");




        session=Session.getDefaultInstance(properties,this);
        session.setDebug(true);
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(user,password);
    }

    public void sendMail(String subject,String id,String sender,String recipients)
            throws Exception
    {


        msg = new MimeMessage(session);
        msg.setSubject(subject);
        Address fromAddr = new InternetAddress(sender); // 보내는사람 EMAIL
        msg.setFrom(fromAddr);
        Address toAddr = new InternetAddress(recipients);    //받는사람 EMAIL
        msg.addRecipient(Message.RecipientType.TO, toAddr);
        msg.setContent("본인인증 메일입니다. <br>" +"<a href='http://jw910911.vps.phps.kr/email_auth.php?id="+id+"'>[인증확인]</a>"
                ,"text/html;charset=utf8");

        new sendmailThread().execute();


    }
    class sendmailThread extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... Voids) {
            try {
                Transport.send(msg);
                Log.d("tranport","성공");
            } catch (MessagingException e) {
                e.printStackTrace();
                Log.d("tranport","실패");

            }
            return null;
        }
    }
}
