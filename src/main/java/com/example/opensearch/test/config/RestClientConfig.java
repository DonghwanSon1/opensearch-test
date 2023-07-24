package com.example.opensearch.test.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class RestClientConfig extends AbstractOpenSearchConfiguration {

  @Bean
  public RestHighLevelClient opensearchClient()  throws RuntimeException {

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    if (true) {
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));
    }

    SSLContext sslContext = null;
    try {
      sslContext = SSLContext.getInstance("TLS");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    try {
      sslContext.init(null, new TrustManager[]{
          new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }
          }
      }, new SecureRandom());
    } catch (KeyManagementException e) {
      throw new RuntimeException(e);
    }

    SSLContext finalSslContext = sslContext;
    RestClientBuilder builder = RestClient.builder(new HttpHost("101.202.40.5", 19200, "https"))
//    RestClientBuilder builder = RestClient.builder(new HttpHost("10.0.11.61", 9200, "https"))
        .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
            .setConnectTimeout(30000)
            .setSocketTimeout(300000))
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
            .setDefaultCredentialsProvider(credentialsProvider)
            .setConnectionReuseStrategy((response, context) -> true)
            .setKeepAliveStrategy(((response, context) -> 300000))
            .setSSLContext(finalSslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));

    return new RestHighLevelClient(builder);
  }
}

