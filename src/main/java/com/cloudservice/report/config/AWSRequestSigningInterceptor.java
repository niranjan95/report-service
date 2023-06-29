package com.cloudservice.report.config;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.Signer;
import com.amazonaws.http.HttpMethodName;
import org.apache.http.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AWSRequestSigningInterceptor implements HttpRequestInterceptor {

   private static final String INVALID_URI = "Invalid URI";

   private static final String ZERO = "0";

   private static final String HOST = "host";

   private static final String CONTENT_LENGTH = "content-length";

   private final String service;

   private final Signer signer;

   private final AWSCredentialsProvider credentialsProvider;

   public AWSRequestSigningInterceptor(final String service, final Signer signer,
         final AWSCredentialsProvider awsCredentialsProvider) {
      this.service = service;
      this.signer = signer;
      this.credentialsProvider = awsCredentialsProvider;
   }

   @Override
   public void process(final HttpRequest httpRequest, final HttpContext httpContext)
         throws HttpException, IOException {
      URIBuilder uriBuilder;
      try {
         uriBuilder = new URIBuilder(httpRequest.getRequestLine().getUri());
      } catch (URISyntaxException e) {
         throw new IOException(INVALID_URI, e);
      }

      DefaultRequest<?> signableRequest = new DefaultRequest<>(service);

      HttpHost targetHost = (HttpHost) httpContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
      if (targetHost != null) {
         signableRequest.setEndpoint(URI.create(targetHost.toURI()));
      }
      final HttpMethodName httpMethod = HttpMethodName.fromValue(httpRequest.getRequestLine().getMethod());
      signableRequest.setHttpMethod(httpMethod);
      try {
         signableRequest.setResourcePath(uriBuilder.build().getRawPath());
      } catch (URISyntaxException e) {
         throw new IOException(INVALID_URI, e);
      }

      if (httpRequest instanceof HttpEntityEnclosingRequest) {
         HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
         if (httpEntityEnclosingRequest.getEntity() != null) {
            signableRequest.setContent(httpEntityEnclosingRequest.getEntity().getContent());
         }
      }
      signableRequest.setParameters(nameValuePairToMap(uriBuilder.getQueryParams()));
      signableRequest.setHeaders(headerArrayToMap(httpRequest.getAllHeaders()));

      signer.sign(signableRequest, credentialsProvider.getCredentials());

      httpRequest.setHeaders(toHeaderArray(signableRequest.getHeaders()));
      if (httpRequest instanceof HttpEntityEnclosingRequest) {
         HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
         if (httpEntityEnclosingRequest.getEntity() != null) {
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpEntity.setContent(signableRequest.getContent());
            httpEntityEnclosingRequest.setEntity(basicHttpEntity);
         }
      }
   }

   private static Map<String, List<String>> nameValuePairToMap(final List<NameValuePair> queryParams) {
      Map<String, List<String>> parameterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      for (NameValuePair nameValuePair : queryParams) {
         List<String> argsList = parameterMap.computeIfAbsent(nameValuePair.getName(), k -> new ArrayList<>());
         argsList.add(nameValuePair.getValue());
      }
      return parameterMap;
   }

   private static Map<String, String> headerArrayToMap(final Header[] headers) {
      Map<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      for (Header header : headers) {
         if (!shallSkipHeader(header)) {
            headersMap.put(header.getName(), header.getValue());
         }
      }
      return headersMap;
   }

   private static boolean shallSkipHeader(final Header header) {
      return (CONTENT_LENGTH.equalsIgnoreCase(header.getName()) && ZERO.equals(header.getValue()))
            || HOST.equalsIgnoreCase(header.getName());
   }

   private static Header[] toHeaderArray(final Map<String, String> mapHeaders) {
      Header[] headers = new Header[mapHeaders.size()];
      int index = 0;
      for (Map.Entry<String, String> headerEntry : mapHeaders.entrySet()) {
         headers[index++] = new BasicHeader(headerEntry.getKey(), headerEntry.getValue());
      }
      return headers;
   }
}