package com.ylpsingapore.app;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class TestMQTT {

	public static void main(String[] args) {
		
		String serverUrl = "ssl://ylptest.ddns.net:8883";
		String caFilePath = "/Users/leipoyan/Downloads/keep/certs2/MyRootCaCert.pem";
		String clientCrtFilePath = "/Users/leipoyan/Downloads/keep/certs2/client1.crt";
		String clientKeyFilePath = "/Users/leipoyan/Downloads/keep/certs2/client1.key";
		String mqttUserName = "default";
		String mqttPassword = "default";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-cip")) {
				serverUrl = "ssl://" + args[++i];
			} else if (args[i].equals("-ca")) {	
				caFilePath = args[++i];
			} else if (args[i].equals("-cu")) {
				mqttUserName = args[++i];
			} else if (args[i].equals("-cp")) {
				mqttPassword = args[++i];
			} else if (args[i].equals("-key")) {
				clientKeyFilePath = args[++i];
			} else if (args[i].equals("-cert")) {
				clientCrtFilePath = args[++i];
			} 
		}
		System.out.println("arg length: " + Integer.toString(args.length));
		System.out.println("serverUrl: " + serverUrl);
		System.out.println("mqttUserName: " + mqttUserName);
		System.out.println("caFilePath: " + caFilePath);
		
		MqttClient client;
		try {
			client = new MqttClient(serverUrl, "2");
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName(mqttUserName);
			options.setPassword(mqttPassword.toCharArray());
			
			options.setConnectionTimeout(60);
			options.setKeepAliveInterval(60);
			options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

			
			SSLSocketFactory socketFactory = getSocketFactory(caFilePath,
					clientCrtFilePath, clientKeyFilePath, "");
			options.setSocketFactory(socketFactory);

			System.out.println("starting connect the server...");
			client.connect(options);
			System.out.println("connected!");
			Thread.sleep(1000);

			client.subscribe(
					"/u/56ca327d17531d08e76bddd4a215e37f5fd6082f7442151c4d3f1d100a0ffd4e",
					0);
			client.disconnect();
			System.out.println("disconnected!");


		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static SSLSocketFactory getSocketFactory(final String caCrtFile,
			final String crtFile, final String keyFile, final String password)
			throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		FileInputStream fis = new FileInputStream(caCrtFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client certificate
		bis = new BufferedInputStream(new FileInputStream(crtFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client private key
		PEMParser pemParser = new PEMParser(new FileReader(keyFile));
		Object object = pemParser.readObject();
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
				.build(password.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
				.setProvider("BC");
		KeyPair key;
		if (object instanceof PEMEncryptedKeyPair) {
			System.out.println("Encrypted key - we will use provided password");
			key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
					.decryptKeyPair(decProv));
		} else {
			System.out.println("Unencrypted key - no password needed");
			key = converter.getKeyPair((PEMKeyPair) object);
		}
		pemParser.close();

		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);

		// client key and certificates are sent to server so it can authenticate
		// us
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
				new java.security.cert.Certificate[] { cert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
				.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());

		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	}

	static void printUsage() {
		System.out.println("-cip <broker_domain_name:port> -ca <cacert> -cu <username> -cp <password> -key <client_key> -cert <client_cert>");
	}
}
