/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package server;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
public class SERVER {

    public static void main(String[] args) {
        final int PORT = 12345; // Cổng UDP
        final String VIDEO_PATH = "C:/Users/HP/Desktop/ct2_Thay hau/VD/video1.mp4"; // Đường dẫn video
        final int MAX_UDP_PACKET_SIZE = 1400; 

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress groupAddress = InetAddress.getByName("239.255.0.1"); // Địa chỉ multicast

            // Khởi tạo FrameGrabber để lấy video từ file
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(VIDEO_PATH);
            grabber.start(); // Bắt đầu grab video

            Frame frame;
            Java2DFrameConverter converter = new Java2DFrameConverter();

            System.out.println("Dang phat video...");

            // Đọc video từng khung hình và gửi qua UDP
            while ((frame = grabber.grab()) != null) {
                BufferedImage bufferedImage = converter.convert(frame);
                if (bufferedImage != null) {
                    // Chuyển đổi BufferedImage thành mảng byte
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();

                    // Tính toán số lượng gói cần gửi
                    int totalLength = imageBytes.length;
                    int totalPackets = (int) Math.ceil((double) totalLength / MAX_UDP_PACKET_SIZE);
                    int offset = 0;
                    int packetIndex = 0;

                    while (offset < totalLength) {
                        int packetSize = Math.min(MAX_UDP_PACKET_SIZE, totalLength - offset);
                        byte[] packetData = new byte[packetSize + 8]; // Thêm 8 byte cho header

                        // Thêm header vào gói
                        System.arraycopy(intToBytes(packetIndex), 0, packetData, 0, 4); // Số thứ tự gói
                        System.arraycopy(intToBytes(totalPackets), 0, packetData, 4, 4); // Tổng số gói

                        // Sao chép dữ liệu hình ảnh vào gói
                        System.arraycopy(imageBytes, offset, packetData, 8, packetSize);

                        // Gửi gói dữ liệu qua UDP
                        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, groupAddress, PORT);
                        socket.send(packet);
// In thông tin về gói đã gửi
                        System.out.println("Da gui goi " + (packetIndex + 1) + "/" + totalPackets + ", kich thuoc: " + packetSize + " bytes");

                        offset += packetSize;
                        packetIndex++;
                    }

                    // Điều chỉnh tốc độ phát video dựa trên frame rate
                    long sleepTime = (long) (1000 / grabber.getFrameRate());
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                }
            }

            grabber.stop(); // Dừng grabber khi video kết thúc
            System.out.println("Video phát xong.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm chuyển int thành byte array
    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }
}