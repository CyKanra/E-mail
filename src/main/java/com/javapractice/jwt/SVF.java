package com.javapractice.jwt;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SVF {
  /**
   * 帳票出力
   * @param args[0] accessToken 例：77eb1e8fe50a0b24f60c8ef5efg131096fbc4329e2f089f82d30f734f2c9a7a07
   * @param args[1] printerId 例："0693i030-83af-47dc-82f8-b1e2065209e6"などのユニークID
   * @param args[2] formFilePath 例：form/Test/sample_ja.xml
   * @param args[3] dataFilePath 例：/WebAPISample/sample_ja.csv
   * @param args[4] resourceFilePath 例：/WebAPISample/logo.png
   * @throws InterruptedException 
   */
  public static void main(String[] args) throws InterruptedException {
    String accessToken = "e468b0f69c05aa25df534b2efab2bef0c97527ba0dbac03c8b22a2b08324c554";
    String printerId = "EXCEL";
    String formFilePath = "form/Demo/demo.xlsx";
//    String formFilePath = "form/Demo/Mytest.xml";
    String dataFilePath = "C:\\LHN\\Project\\sub.csv";
    String resourceFilePath = "";

    // 帳票出力
    String location = SVF.print(accessToken, printerId, formFilePath, dataFilePath, resourceFilePath);
    if (location != null) {
      System.out.println("location=\n" + location);
    } else {
      System.out.println("print error.");
      return;
    }
    
    // 印刷が完了するくらいの時間を設定
    Thread.sleep(5000);

    // 印刷状況の取得
    String printStatus = SVF.retrievePrintStatus(accessToken, location);
    System.out.println("printStatus=\n" + printStatus);
  }

  private static final String API_ENDPOINT = "https://api.svfcloud.com/";// APIを利用するためのドメイン
  private static final String ARTIFACTS_URI = "v1/artifacts";// 帳票の成果物を操作するエンドポイント URI
  private static final String CRLF = "\r\n";// 改行

  /**
   * 帳票出力
   * @param accessToken
   * @param printerId
   * @param formFilePath
   * @param dataFilePath
   * @param resourceFilePath
   * @return
   */
  public static String print(String accessToken, String printerId, String formFilePath, String dataFilePath, String resourceFilePath) {
    HttpURLConnection conn = null;
    String location = null;
    try {
      String boundary = Long.toString(System.currentTimeMillis());
      conn = createPostConnection(ARTIFACTS_URI, "multipart/form-data", boundary, accessToken);

      OutputStream outputStream = conn.getOutputStream();
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
      // リクエストデータの作成
      writeFormData(writer, boundary, "name", "sub");// 文書名を指定
      writeFormData(writer, boundary, "source", "CSV");// 帳票データタイプを指定します。"CSV"固定で指定してください
      if (printerId != null) {
        writeFormData(writer, boundary, "printer", printerId);// 出力先プリンターのID
      }
      writeFormData(writer, boundary, "defaultForm", formFilePath);// WebDesigner上のパスを指定します。"form/"で始まるパスを指定します。フォーム名の最後は".xml"を付与してください。
//      writeFormData(writer, boundary, "password", "user");// PDFに対するパスワードを指定
//      writeFormData(writer, boundary, "inputTray", "3");// 給紙トレイを指定
//      writeFormData(writer, boundary, "pdfPermPass", "owner");// 権限パスワードを指定
      writeFormData(writer, boundary, "pdfPermPrint", "high");// PDF印刷許可を指定
      writeFormData(writer, boundary, "pdfPermModify", "assembly");// PDF変更許可を指定
      writeFormData(writer, boundary, "pdfPermCopy", "true");// PDFコピー許可を指定
      writeFormData(writer, boundary, "redirect", "false");// リダイレクト動作を指定
      
      // CSVデータ
      File datafile = new File(dataFilePath);
      writeFileData(writer, outputStream, boundary, URLEncoder.encode("data/"/* "PDFプロパティのタイトルに適用されます。例)発注書"。 */, "UTF-8"), datafile.getName(), datafile);// CSVファイル

      // イメージデータ
//      File resourceFile = new File(resourceFilePath);
//      writeFileData(writer, outputStream, boundary, URLEncoder.encode("resource/", "UTF-8"), resourceFile.getName(), resourceFile);// イメージファイル
      
      writer.append("--").append(boundary).append("--").append(CRLF);
      
      // 帳票出力の実行
      writer.close();

      int responseCode = conn.getResponseCode();
      // 印刷の場合には、202、ファイルダウンロードの場合には、303のステータスコードが返ります。
      if (responseCode == HttpURLConnection.HTTP_ACCEPTED/* 202 */ || responseCode == HttpURLConnection.HTTP_SEE_OTHER/* 303 */) {
        System.out.println(String.format("[%d]", responseCode));
        location = conn.getHeaderField("Location");
        // locationには、
        // 印刷の場合->リクエストの結果から印刷状況を確認するURLが入ります。
        // ファイルダウンロードの場合->リクエストの結果からダウンロード先のURLが入ります。
      } else {
        System.out.println(String.format("[%d][%s]", responseCode, conn.getResponseMessage()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return location;
  }

  /**
   * 印刷状況の取得
   * @param accessToken
   * @param location
   * @return 印刷状況
   */
  public static String retrievePrintStatus(String accessToken, String location) {
    String printerInfo = null;
    HttpURLConnection conn = null;

    try {
      conn = createGetConnection(location, "application/json", accessToken);

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // リクエストの結果から印刷状況を取り出します。
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
   * Postリクエスト用HTTPコネクションの作成
   * @param endPoint
   * @param accessToken
   * @return HttpURLConnection
   * @throws IOException
   */
  private static HttpURLConnection createPostConnection(String endPoint, String contentType, String boundary, String accessToken) throws IOException {
    URL url = null;
    url = new URL(API_ENDPOINT + endPoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    conn.setAllowUserInteraction(false);
    // Httpリクエストヘッダーの設定
    conn.setRequestProperty("Content-Type", contentType + "; boundary=" + boundary);
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    // chunked HTTP streaming mode 
    conn.setChunkedStreamingMode(0);
    return conn;
  }

  /**
   * Getリクエスト用HTTPコネクションの作成
   * @param endPoint
   * @param accessToken
   * @return HttpURLConnection
   * @throws IOException
   */
  private static HttpURLConnection createGetConnection(String endPoint, String accept, String accessToken) throws IOException {
    URL url = null;
    url = new URL(endPoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Httpリクエストヘッダーの設定
    conn.setRequestProperty("Accept", accept);
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    return conn;
  }

  /**
   * フォームデータの設定
   * @param writer
   * @param boundary
   * @param name
   * @param value
   * @throws IOException
   */
  private static void writeFormData(PrintWriter writer, String boundary, String name, String value) throws IOException {
    writer.append("--").append(boundary).append(CRLF)
    .append("Content-Disposition: form-data; name=\"").append(name)
    .append("\"").append(CRLF)
    .append("Content-Type: text/plain; charset=").append("UTF-8")
    .append(CRLF).append(CRLF).append(value).append(CRLF);
  }

  /**
   * アップロードファイルデータの設定
   * @param writer
   * @param outputStream
   * @param boundary
   * @param name サーバー側で一時保存するディレクトリ名（任意にディレクトリ名を指定してください。）
   * @param value
   * @param file
   * @throws IOException
   */
  private static void writeFileData(PrintWriter writer, OutputStream outputStream, String boundary, String name, String value, File file) throws IOException {
    writer.append("--").append(boundary).append(CRLF)
    .append("Content-Disposition: form-data; name=\"")
    .append(name).append("\"; filename=\"").append(value)
    .append("\"").append(CRLF).append("Content-Type: ")
    .append(getContentTypeName(file)).append(CRLF)
    .append("Content-Transfer-Encoding: binary").append(CRLF)
    .append(CRLF);
    
    writer.flush();
    outputStream.flush();

    try (FileInputStream in = new FileInputStream(file)) {
      int len ;
      byte[] b = new byte[8192];
      while ((len = in.read(b)) != -1) {
        outputStream.write(b, 0, len);
      }
      outputStream.flush();
    }
    writer.append(CRLF);
  }
  
  private static String getContentTypeName(File file) throws IOException {
    String path = file.getPath().toLowerCase();
    if (path.endsWith(".png")) {
      return "image/png";
    } else if (path.endsWith(".jpg")) {
      return "image/jpeg";
    } else if (path.endsWith(".bmp")) {
      return "image/bmp";
    } else {
      return "application/octet-stream";
    }
  }

}