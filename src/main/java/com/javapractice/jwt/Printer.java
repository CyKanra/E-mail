package com.javapractice.jwt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Printer {

  /**
   * プリンター一覧の取得
   * @param args[0] accessToken
   */
  public static void main(String[] args) {
    String accessToken = "e7912abac652a2774f4ba7b1e374c5eb58ce6752be60144abf277f2fb3193de5";
    System.out.println("------ 仮登録状態のプリンター一覧を取得します。-------");
    String printerListJsonString = Printer.retrieveAllPrinter(accessToken, /* registerable */true);
    System.out.println(printerListJsonString);

    System.out.println("------ 登録済みのプリンター一覧を取得します。-------");
    printerListJsonString = Printer.retrieveAllPrinter(accessToken, /* registerable */false);
    System.out.println(printerListJsonString);

    System.out.println("------ プリンターを登録します。 -------");
    String printerKey = "889757F53BD90F8777F69EAB42971EC8138A612C";
    String location = registerPrinter(accessToken, printerKey);
    System.out.println(location);
    
    if (location == null) {
      System.out.println("location is null.");
      return;
    }
      System.out.println("------ プリンター情報を取得します。 -------");
      String printerId = getPrinterId(location);// 登録したプリンターIDを取得する
      String printerInfo = retrievePrinter(accessToken, printerId);
      System.out.println(printerInfo);

      System.out.println("------ プリンターの登録を解除します。 -------");
      unregisterPrinter(accessToken, printerId);
  }

  private static final String API_ENDPOINT = "https://api.svfcloud.com/";// APIを利用するためのドメイン
  private static final String PRINTERS_URI = "v1/printers";// プリンター情報を操作するエンドポイント URI

  /**
   * プリンター一覧の取得
   * @param accessToken アクセストークン
   * @param registerable (false：登録済み（デフォルト）、true：未登録)
   * @return printerListJsonString プリンタ一覧のJSONデータ
   */
  private static String retrieveAllPrinter(String accessToken, boolean registerable) {
    String printerListJsonString = null;
    HttpURLConnection conn = null;

    try {
      String queryParams = "registerable=" + registerable;
      conn = createGetConnection(PRINTERS_URI, "application/json", accessToken, queryParams);

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // リクエストの結果からプリンター情報(JSON)を取り出します。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
          printerListJsonString = reader.readLine();
        }
      } else {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return printerListJsonString;
  }

  /**
   * プリンターを登録する
   * @param accessToken
   * @param key
   * @return
   */
  private static String registerPrinter(String accessToken, String key) {
    String location = null;
    HttpURLConnection conn = null;
    try {
      conn = createPostConnection(PRINTERS_URI, "application/x-www-form-urlencoded", accessToken);

      // リクエスト送信
      Writer writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
      writer.write("key=" + key);
      writer.close();

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_CREATED) {
        // プリンター登録が成功するとプリンター情報取得用のURIが返ります。
        location = conn.getHeaderField("Location");
      } else {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return location;
  }

  /**
   * プリンター情報の取得
   * @param accessToken
   * @param printerId
   * @return プリンター情報
   */
  private static String retrievePrinter(String accessToken, String printerId) {
    String printerInfo = null;
    HttpURLConnection conn = null;

    try {
      conn = createGetConnection(PRINTERS_URI + "/" + printerId, "application/json", accessToken, null);

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // リクエストの結果からプリンター情報(JSON)を取り出します。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
          printerInfo = reader.readLine();
        }
      } else {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return printerInfo;
  }

  /**
   * プリンターの登録解除
   * 
   * @param accessToken
   * @param printerId
   */
  private static void unregisterPrinter(String accessToken, String printerId) {
    HttpURLConnection conn = null;
    try {
      // 削除対象のプリンターIDを設定
      conn = createDeleteConnection(PRINTERS_URI + "/" + printerId, "application/x-www-form-urlencoded", accessToken);

      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      } else {
        System.out.println("DELETE OK. " + printerId);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Getリクエスト用HTTPコネクションの作成
   * @param endPoint
   * @param accessToken
   * @param queryParams
   * @return HttpURLConnection
   * @throws IOException
   */
  private static HttpURLConnection createGetConnection(String endPoint, String contentType, String accessToken, String queryParams) throws IOException {
    URL url = null;
    url = new URL(API_ENDPOINT + endPoint + (queryParams != null ? "?" + queryParams : ""));
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Httpリクエストヘッダーの設定
    conn.setRequestProperty("Content-Type", contentType);
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    return conn;
  }

  /**
   * Postリクエスト用HTTPコネクションの作成
   * @param endPoint
   * @param accessToken
   * @param queryParams
   * @return HttpURLConnection
   * @throws IOException
   */
  private static HttpURLConnection createPostConnection(String endPoint, String contentType, String accessToken) throws IOException {
    URL url = null;
    url = new URL(API_ENDPOINT + endPoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    conn.setAllowUserInteraction(false);
    // Httpリクエストヘッダーの設定
    conn.setRequestProperty("Content-Type", contentType);
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    return conn;
  }

  /**
   * Deleteリクエスト用HTTPコネクションの作成
   * @param endPoint
   * @param accessToken
   * @param queryParams
   * @return HttpURLConnection
   * @throws IOException
   */
  private static HttpURLConnection createDeleteConnection(String endPoint, String contentType, String accessToken) throws IOException {
    URL url = null;
    url = new URL(API_ENDPOINT + endPoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("DELETE");
    conn.setDoInput(true);
    conn.setUseCaches(false);
    // Httpリクエストヘッダーの設定
    conn.setRequestProperty("Content-Type", contentType);
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    return conn;
  }

  /**
   * プリンター登録時のロケーションデータからプリンターIDの取得
   * @param location
   * @return プリンターID
   */
  private static String getPrinterId(String location) {
    return location.replace(API_ENDPOINT + PRINTERS_URI + "/", "");
  }
}