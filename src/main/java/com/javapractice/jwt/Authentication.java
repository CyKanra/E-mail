package com.javapractice.jwt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Authentication {
  /**
   * アクセストークンの取得と破棄
   * ※このプログラムは、Java8で動作します。
   * @param args[0] expDate 例：300（秒）
   * @param args[1] clientId 例：SVFXPDYPJGWIPZJNNCKBZOWOPIORLPIC
   * @param args[2] secret 例：kFVW5xuqVkuWlk9f1RUuUqLImrdoBhEgu4Ng7ltl8OKgFYQpv52zdmU4ViKE2xDT
   * @param args[3] keyFilePath 例：/WebAPI/SVFXPDYPJGWIPZJNNCKBZOWOPIORLPIC.pkcs8
   * @param args[4] userId 例：sakimoto.t
   * @param args[5] userName 例：帳票太郎
   * @param args[6] timeZone 例：Asia/Tokyo (東京)
   */
  public static void main(String[] args) {
    String expDate = "1729849804";
    String clientId = "SVFOQVZUUGUVBCJEUFWJUMUGMCFHINLE";
    String secret = "yWhaWT9JM3W2wppgFeSGscHBpxgokP2koqPdrMiq6z7czBpFWLBcSQIfUnPN3zXC";
    String keyFilePath = "D:\\client.pkcs8";
    String userId = "administrator";
    String userName = "administrator";
    String timeZone = "Asia/Tokyo";

    // JWT ベアラートークン（認証用トークン）を生成します。
    String jwtToken = Authentication.generateJWTBearerToken(clientId, userId, keyFilePath, Integer.valueOf(expDate), userName, timeZone);
    System.out.println("jwtToken=\n" + jwtToken);

    // アクセストークンを取得します。
    String accessToken = Authentication.getAccessToken(jwtToken, clientId, secret);
    System.out.println("accessToken=\n" + accessToken);

    // アクセストークンを破棄します。
    // Authentication.revokeAccessToken(accessToken);
  }

  private static final String API_DOMAIN = "https://api.svfcloud.com/";// APIを利用するためのドメイン
  private static final String OAUTH2_TOKEN_URI = "oauth2/token"; // アクセストークン取得のエンドポイント URI
  private static final String OAUTH2_REVOKE_URI = "oauth2/revoke";// アクセストークン破棄のエンドポイント URI
  private static final String RSA_ALGORITHM = "{\"alg\":\"RS256\"}";// 署名作成アルゴリズム（RSA using SHA-256 hash）
  private static final String CLAIM_TEMPLATE = "'{'\"iss\": \"{0}\", \"sub\": \"{1}\", \"exp\": \"{2}\", \"userName\": \"{3}\", \"timeZone\": \"{4}\"'}'";// 要求セットのテンプレート
  private static final String GRANT_TYPE_JWT_BEARER = "urn:ietf:params:oauth:grant-type:jwt-bearer";// アクセストークン取得の認可タイプの値
  private static final String PKCS8_HEADER = "-----BEGIN PRIVATE KEY-----";// 秘密鍵データヘッダー
  private static final String PKCS8_FOOTER = "-----END PRIVATE KEY-----";// 秘密鍵データフッター

  /**
   * 認証用トークンの作成
   * 
   * @param clientId
   * @param userId
   * @param keyFilePath
   * @param userName
   * @param timeZone
   * @return
   */
  private static String generateJWTBearerToken(String clientId, String userId, String keyFilePath, int expDate, String userName, String timeZone) {
    StringBuffer jwtToken = new StringBuffer();
    try {
      // 署名作成アルゴリズムを指定します。
      String header = Base64.getEncoder().encodeToString(RSA_ALGORITHM.getBytes("UTF-8"));

      /* 要求セットの作成 */
      String payload = Base64.getEncoder().encodeToString(createPayload(clientId, userId, expDate, userName, timeZone).getBytes("UTF-8"));

      /* 署名対象のデータ */
      jwtToken.append(header).append(".").append(payload);

      /* 署名データを作成 */
      byte[] signedBytes = signData(keyFilePath, jwtToken.toString());
      String signedString = Base64.getEncoder().encodeToString(signedBytes);

      /* 署名データを付与して、JWTトークンの作成完了 */
      jwtToken.append(".").append(signedString);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jwtToken.toString();
  }

  /**
   * アクセストークンの取得
   * @param jwtToken
   * @param clientId
   * @param secret
   * @return
   */
  private static String getAccessToken(String jwtToken, String clientId, String secret) {
    String accessToken = null;
    HttpURLConnection conn = null;
    try {
      conn = createHttpPostConnection(OAUTH2_TOKEN_URI, "application/x-www-form-urlencoded");

      // httpヘッダーの作成
      String authHeader = Base64.getEncoder().encodeToString((clientId + ":" + secret).getBytes());
      conn.setRequestProperty("Authorization", "Basic " + authHeader);

      // パラメータ作成
      Map<String, String> paramMap = new HashMap<>();
      paramMap.put("assertion", jwtToken);
      paramMap.put("grant_type", GRANT_TYPE_JWT_BEARER);
      String postParam = createPostParameter(paramMap);

      // リクエスト送信
      Writer writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
      writer.write(postParam);// Postパラメータを設定
      writer.close();

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // リクエストの結果からアクセストークンを取り出します。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
          String result = reader.readLine();
          accessToken = result.replace("{\"token\":\"", "").replaceAll("expiration.*", "").replace("\",\"", "");
        }
      } else {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return accessToken;
  }

  /**
   * アクセストークンの破棄
   * @param accessToken
   */
  private static void revokeAccessToken(String accessToken) {
    HttpURLConnection conn = null;
    try {
      conn = createHttpPostConnection(OAUTH2_REVOKE_URI, "application/x-www-form-urlencoded");
      // httpヘッダーの作成
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);

      Writer writer;
      writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
      writer.write("token=" + accessToken);
      writer.close();
      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      }
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Postリクエスト用HTTPコネクションの作成
   * @param endPoint
   * @return HttpURLConnection
   * @throws IOException
   */
  private static HttpURLConnection createHttpPostConnection(String endPoint, String contentType) throws IOException {
    URL url = new URL(API_DOMAIN + endPoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    conn.setAllowUserInteraction(false);
    conn.setRequestProperty("Content-Type", contentType);

    return conn;
  }

  /**
   * 要求セットの作成
   * @param clientId
   * @param userId
   * @param expDate
   * @param userName
   * @param timeZone
   * @return 要求セット
   * @throws UnsupportedEncodingException
   */
  private static String createPayload(String clientId, String userId, int expDate, String userName, String timeZone) throws UnsupportedEncodingException {
    String[] claimArray = new String[5];
    claimArray[0] = clientId;
    claimArray[1] = userId;
    long now = System.currentTimeMillis() / 1000L;
    claimArray[2] = Long.toString(now + expDate);
    claimArray[3] = userName;
    claimArray[4] = timeZone;
    MessageFormat claims;
    claims = new MessageFormat(CLAIM_TEMPLATE);
    return claims.format(claimArray);
  }

  /**
   * 署名用の秘密鍵を生成
   * @param keyFilePath
   * @return 秘密鍵
   * @throws FileNotFoundException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  private static RSAPrivateKey createPrivateKey(String keyFilePath) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    // 秘密鍵ファイルの読み込み
    StringBuilder sbPrivateKey = new StringBuilder();
    File privKeyFile = new File(keyFilePath);
    try (BufferedReader br = new BufferedReader(new FileReader(privKeyFile))) {
      String privateKey = "";
      while ((privateKey = br.readLine()) != null) {
        if (privateKey.contains(PKCS8_HEADER)) {
          privateKey = privateKey.replaceAll(PKCS8_HEADER, "");
        }
        if (privateKey.contains(PKCS8_FOOTER)) {
          privateKey = privateKey.replaceAll(PKCS8_FOOTER, "");
        }
        sbPrivateKey.append(privateKey.trim());
      }
    }
    byte[] privKeyBytes = sbPrivateKey.toString().getBytes();
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKeyBytes));
    return (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
  }

  /**
   * 署名データ作成
   * @param keyFilePath
   * @param signStr
   * @return 署名済みデータ
   * @throws NoSuchAlgorithmException
   * @throws FileNotFoundException
   * @throws InvalidKeySpecException
   * @throws IOException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  private static byte[] signData(String keyFilePath, String signStr) throws NoSuchAlgorithmException, FileNotFoundException, InvalidKeySpecException, IOException, InvalidKeyException, SignatureException {
    Signature signature = Signature.getInstance("SHA256withRSA");
    RSAPrivateKey privateKey = createPrivateKey(keyFilePath);
    signature.initSign(privateKey);
    signature.update(signStr.getBytes("UTF-8"));
    return signature.sign();
  }

  /**
   * Postパラメータ生成
   * @param paramMap
   * @return パラメータ
   * @throws UnsupportedEncodingException
   */
  private static String createPostParameter(Map<String, String> paramMap) throws UnsupportedEncodingException {
    StringBuilder sbPostParam = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      if (first) {
        first = false;
      } else {
        sbPostParam.append("&");
      }
      sbPostParam.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
      sbPostParam.append("=");
      sbPostParam.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
    }
    return sbPostParam.toString();
  }
}