/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2015-01-14 17:53:03 UTC)
 * on 2015-02-18 at 07:53:38 UTC 
 * Modify at your own risk.
 */

package com.myapp.flarebackend.sendflare;

/**
 * Service definition for Sendflare (v1).
 *
 * <p>
 * This is an API
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link SendflareRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class Sendflare extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.19.1 of the sendflare library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://mineral-actor-720.appspot.com/_ah/api/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "sendflare/v1/";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   */
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport, which should normally be:
   *        <ul>
   *        <li>Google App Engine:
   *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
   *        <li>Android: {@code newCompatibleTransport} from
   *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
   *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
   *        </li>
   *        </ul>
   * @param jsonFactory JSON factory, which may be:
   *        <ul>
   *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
   *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
   *        <li>Android Honeycomb or higher:
   *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
   *        </ul>
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public Sendflare(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  Sendflare(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * Create a request for the method "findDevice".
   *
   * This request holds the parameters needed by the sendflare server.  After setting any optional
   * parameters, call the {@link FindDevice#execute()} method to invoke the remote operation.
   *
   * @param phoneNumber
   * @return the request
   */
  public FindDevice findDevice(java.lang.String phoneNumber) throws java.io.IOException {
    FindDevice result = new FindDevice(phoneNumber);
    initialize(result);
    return result;
  }

  public class FindDevice extends SendflareRequest<com.myapp.flarebackend.sendflare.model.RegisteredDevice> {

    private static final String REST_PATH = "findDevice/{phoneNumber}";

    /**
     * Create a request for the method "findDevice".
     *
     * This request holds the parameters needed by the the sendflare server.  After setting any
     * optional parameters, call the {@link FindDevice#execute()} method to invoke the remote
     * operation. <p> {@link
     * FindDevice#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param phoneNumber
     * @since 1.13
     */
    protected FindDevice(java.lang.String phoneNumber) {
      super(Sendflare.this, "POST", REST_PATH, null, com.myapp.flarebackend.sendflare.model.RegisteredDevice.class);
      this.phoneNumber = com.google.api.client.util.Preconditions.checkNotNull(phoneNumber, "Required parameter phoneNumber must be specified.");
    }

    @Override
    public FindDevice setAlt(java.lang.String alt) {
      return (FindDevice) super.setAlt(alt);
    }

    @Override
    public FindDevice setFields(java.lang.String fields) {
      return (FindDevice) super.setFields(fields);
    }

    @Override
    public FindDevice setKey(java.lang.String key) {
      return (FindDevice) super.setKey(key);
    }

    @Override
    public FindDevice setOauthToken(java.lang.String oauthToken) {
      return (FindDevice) super.setOauthToken(oauthToken);
    }

    @Override
    public FindDevice setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (FindDevice) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public FindDevice setQuotaUser(java.lang.String quotaUser) {
      return (FindDevice) super.setQuotaUser(quotaUser);
    }

    @Override
    public FindDevice setUserIp(java.lang.String userIp) {
      return (FindDevice) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String phoneNumber;

    /**

     */
    public java.lang.String getPhoneNumber() {
      return phoneNumber;
    }

    public FindDevice setPhoneNumber(java.lang.String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    @Override
    public FindDevice set(String parameterName, Object value) {
      return (FindDevice) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "findDeviceById".
   *
   * This request holds the parameters needed by the sendflare server.  After setting any optional
   * parameters, call the {@link FindDeviceById#execute()} method to invoke the remote operation.
   *
   * @param regId
   * @return the request
   */
  public FindDeviceById findDeviceById(java.lang.String regId) throws java.io.IOException {
    FindDeviceById result = new FindDeviceById(regId);
    initialize(result);
    return result;
  }

  public class FindDeviceById extends SendflareRequest<com.myapp.flarebackend.sendflare.model.RegisteredDevice> {

    private static final String REST_PATH = "findDeviceById/{regId}";

    /**
     * Create a request for the method "findDeviceById".
     *
     * This request holds the parameters needed by the the sendflare server.  After setting any
     * optional parameters, call the {@link FindDeviceById#execute()} method to invoke the remote
     * operation. <p> {@link FindDeviceById#initialize(com.google.api.client.googleapis.services.Abstr
     * actGoogleClientRequest)} must be called to initialize this instance immediately after invoking
     * the constructor. </p>
     *
     * @param regId
     * @since 1.13
     */
    protected FindDeviceById(java.lang.String regId) {
      super(Sendflare.this, "POST", REST_PATH, null, com.myapp.flarebackend.sendflare.model.RegisteredDevice.class);
      this.regId = com.google.api.client.util.Preconditions.checkNotNull(regId, "Required parameter regId must be specified.");
    }

    @Override
    public FindDeviceById setAlt(java.lang.String alt) {
      return (FindDeviceById) super.setAlt(alt);
    }

    @Override
    public FindDeviceById setFields(java.lang.String fields) {
      return (FindDeviceById) super.setFields(fields);
    }

    @Override
    public FindDeviceById setKey(java.lang.String key) {
      return (FindDeviceById) super.setKey(key);
    }

    @Override
    public FindDeviceById setOauthToken(java.lang.String oauthToken) {
      return (FindDeviceById) super.setOauthToken(oauthToken);
    }

    @Override
    public FindDeviceById setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (FindDeviceById) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public FindDeviceById setQuotaUser(java.lang.String quotaUser) {
      return (FindDeviceById) super.setQuotaUser(quotaUser);
    }

    @Override
    public FindDeviceById setUserIp(java.lang.String userIp) {
      return (FindDeviceById) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String regId;

    /**

     */
    public java.lang.String getRegId() {
      return regId;
    }

    public FindDeviceById setRegId(java.lang.String regId) {
      this.regId = regId;
      return this;
    }

    @Override
    public FindDeviceById set(String parameterName, Object value) {
      return (FindDeviceById) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "sendFlare".
   *
   * This request holds the parameters needed by the sendflare server.  After setting any optional
   * parameters, call the {@link SendFlare#execute()} method to invoke the remote operation.
   *
   * @param regId
   * @param phoneNumber
   * @param latitude
   * @param longitude
   * @param text
   * @return the request
   */
  public SendFlare sendFlare(java.lang.String regId, java.lang.String phoneNumber, java.lang.String latitude, java.lang.String longitude, java.lang.String text) throws java.io.IOException {
    SendFlare result = new SendFlare(regId, phoneNumber, latitude, longitude, text);
    initialize(result);
    return result;
  }

  public class SendFlare extends SendflareRequest<Void> {

    private static final String REST_PATH = "sendFlare/{regId}/{phoneNumber}/{latitude}/{longitude}/{text}";

    /**
     * Create a request for the method "sendFlare".
     *
     * This request holds the parameters needed by the the sendflare server.  After setting any
     * optional parameters, call the {@link SendFlare#execute()} method to invoke the remote
     * operation. <p> {@link
     * SendFlare#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param regId
     * @param phoneNumber
     * @param latitude
     * @param longitude
     * @param text
     * @since 1.13
     */
    protected SendFlare(java.lang.String regId, java.lang.String phoneNumber, java.lang.String latitude, java.lang.String longitude, java.lang.String text) {
      super(Sendflare.this, "POST", REST_PATH, null, Void.class);
      this.regId = com.google.api.client.util.Preconditions.checkNotNull(regId, "Required parameter regId must be specified.");
      this.phoneNumber = com.google.api.client.util.Preconditions.checkNotNull(phoneNumber, "Required parameter phoneNumber must be specified.");
      this.latitude = com.google.api.client.util.Preconditions.checkNotNull(latitude, "Required parameter latitude must be specified.");
      this.longitude = com.google.api.client.util.Preconditions.checkNotNull(longitude, "Required parameter longitude must be specified.");
      this.text = com.google.api.client.util.Preconditions.checkNotNull(text, "Required parameter text must be specified.");
    }

    @Override
    public SendFlare setAlt(java.lang.String alt) {
      return (SendFlare) super.setAlt(alt);
    }

    @Override
    public SendFlare setFields(java.lang.String fields) {
      return (SendFlare) super.setFields(fields);
    }

    @Override
    public SendFlare setKey(java.lang.String key) {
      return (SendFlare) super.setKey(key);
    }

    @Override
    public SendFlare setOauthToken(java.lang.String oauthToken) {
      return (SendFlare) super.setOauthToken(oauthToken);
    }

    @Override
    public SendFlare setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (SendFlare) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public SendFlare setQuotaUser(java.lang.String quotaUser) {
      return (SendFlare) super.setQuotaUser(quotaUser);
    }

    @Override
    public SendFlare setUserIp(java.lang.String userIp) {
      return (SendFlare) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String regId;

    /**

     */
    public java.lang.String getRegId() {
      return regId;
    }

    public SendFlare setRegId(java.lang.String regId) {
      this.regId = regId;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.String phoneNumber;

    /**

     */
    public java.lang.String getPhoneNumber() {
      return phoneNumber;
    }

    public SendFlare setPhoneNumber(java.lang.String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.String latitude;

    /**

     */
    public java.lang.String getLatitude() {
      return latitude;
    }

    public SendFlare setLatitude(java.lang.String latitude) {
      this.latitude = latitude;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.String longitude;

    /**

     */
    public java.lang.String getLongitude() {
      return longitude;
    }

    public SendFlare setLongitude(java.lang.String longitude) {
      this.longitude = longitude;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.String text;

    /**

     */
    public java.lang.String getText() {
      return text;
    }

    public SendFlare setText(java.lang.String text) {
      this.text = text;
      return this;
    }

    @Override
    public SendFlare set(String parameterName, Object value) {
      return (SendFlare) super.set(parameterName, value);
    }
  }

  /**
   * Builder for {@link Sendflare}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport, which should normally be:
     *        <ul>
     *        <li>Google App Engine:
     *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
     *        <li>Android: {@code newCompatibleTransport} from
     *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
     *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
     *        </li>
     *        </ul>
     * @param jsonFactory JSON factory, which may be:
     *        <ul>
     *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
     *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
     *        <li>Android Honeycomb or higher:
     *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
     *        </ul>
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
    }

    /** Builds a new instance of {@link Sendflare}. */
    @Override
    public Sendflare build() {
      return new Sendflare(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }

    /**
     * Set the {@link SendflareRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setSendflareRequestInitializer(
        SendflareRequestInitializer sendflareRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(sendflareRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}
