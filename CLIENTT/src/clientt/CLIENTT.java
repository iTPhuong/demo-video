/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package clientt;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;

public class CLIENTT extends Application {

    private static final String MULTICAST_ADDRESS = "239.255.0.1";
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1408; // 1400 bytes + 8 bytes header

    private ImageView imageView;
    private Label frameCounterLabel;
    private int frameCounter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Tạo ImageView để hiển thị hình ảnh
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);

        // Tạo Label để hiển thị số lượng frames
        frameCounterLabel = new Label("Frames received: 0");
        frameCounterLabel.setStyle("-fx-text-fill: white;");
        frameCounterLabel.setLayoutX(10);
        frameCounterLabel.setLayoutY(10);

        // Sử dụng Pane để chứa ImageView và Label
        Pane root = new Pane();
        root.getChildren().addAll(imageView, frameCounterLabel);

        // Tạo Scene và thêm vào Stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Video Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Khởi chạy luồng nhận và hiển thị hình ảnh
        Thread receiveThread = new Thread(this::startReceiving);
        receiveThread.setDaemon(true); // Đảm bảo thread dừng khi ứng dụng đóng
        receiveThread.start();
    }

    private void startReceiving() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress groupAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(groupAddress);

            byte[] buffer = new byte[BUFFER_SIZE];
            System.out.println("Dang nhan video...");

            Map<Integer, byte[]> packetMap = new HashMap<>();
            int expectedPackets = -1;

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] receivedData = packet.getData();
                int packetIndex = bytesToInt(receivedData, 0);
                int totalPackets = bytesToInt(receivedData, 4);

                if (expectedPackets == -1) {
                    expectedPackets = totalPackets;
                    packetMap.clear();
                }
byte[] imageData = new byte[packet.getLength() - 8];
                System.arraycopy(receivedData, 8, imageData, 0, imageData.length);
                packetMap.put(packetIndex, imageData);

                // Kiểm tra xem đã nhận đủ gói để ghép hình ảnh chưa
                if (packetMap.size() == expectedPackets) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int i = 0; i < expectedPackets; i++) {
                        baos.write(packetMap.get(i));
                    }

                    byte[] fullImageBytes = baos.toByteArray();
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Image image = decodeImage(fullImageBytes);
                            if (image != null) {
                                imageView.setImage(image);
                                frameCounter++;
                                frameCounterLabel.setText("Frames received: " + frameCounter);
                            }
                        } catch (Exception e) {
                            System.out.println("Lỗi khi hiển thị hình ảnh.");
                            e.printStackTrace();
                        }
                    });

                    // Reset trạng thái để nhận khung hình tiếp theo
                    expectedPackets = -1;
                    packetMap.clear();
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi kết nối hoặc xử lý gói tin.");
            e.printStackTrace();
        }
    }

    private Image decodeImage(byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            return new Image(inputStream);
        } catch (Exception e) {
            System.out.println("Có lỗi xảy ra khi giải mã hình ảnh.");
            e.printStackTrace();
        }
        return null;
    }

    private int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
               ((bytes[offset + 1] & 0xFF) << 16) |
               ((bytes[offset + 2] & 0xFF) << 8) |
               (bytes[offset + 3] & 0xFF);
    }
}