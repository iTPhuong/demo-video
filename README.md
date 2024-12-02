Phát video trong mạng LAN
Video để ở máy phát (chạy chương trình phát video, các máy join vào group này có thể xem video đó, việc phát này giống như đang livestream trên máy phát)
Yêu cầu thực hành thử nghiệm trên 3 máy trong cùng mạng LAN. Máy số 1 là máy phát video, 2 máy còn lại xem video.
1. Đọc và chia nhỏ video
•	Sử dụng thư viện như JavaCV (dựa trên FFmpeg) để đọc video và trích xuất từng khung hình hoặc từng đoạn video nhỏ.
•	Cắt video thành các đoạn ngắn, hoặc thành từng khung hình (nếu cần) và lưu tạm vào bộ nhớ hoặc lưu tạm ra đĩa.
2. Chuyển đổi dữ liệu video thành dạng byte
•	Sau khi có các đoạn video nhỏ, bạn cần chuyển đổi chúng thành chuỗi byte (mảng byte) để có thể gửi qua UDP.
•	Dùng các lớp như ByteArrayOutputStream và DataOutputStream trong Java để hỗ trợ chuyển đổi.
3. Gửi dữ liệu qua UDP
•	Sử dụng DatagramSocket và DatagramPacket để gửi các đoạn byte qua UDP.
•	Đảm bảo thiết lập đúng kích thước gói tin để tránh vấn đề phân mảnh, thường là khoảng 1400-1500 byte mỗi gói để đảm bảo gói không bị chia nhỏ trong quá trình truyền.
•	Mỗi gói tin cần bao gồm thông tin về thứ tự gói để bên nhận có thể sắp xếp lại thành video ban đầu.
4. Bên nhận video
•	Thiết lập một DatagramSocket trên máy nhận để nhận các gói tin UDP.
•	Ghép các gói tin dựa trên thứ tự đã gửi để tạo lại video.
•	Sử dụng lại thư viện JavaCV để tạo lại video từ các đoạn nhỏ hoặc từ các khung hình.
•	Dùng 1 thư viện Player trên JavaFX để hiển thị video bên máy nhận.
Các giải pháp thư viện hỗ trợ
1.	JavaCV (dựa trên FFmpeg): Giúp đọc, xử lý, và lưu video từ các đoạn hoặc từ các khung hình.
2.	JCodec: Có thể hữu ích cho các tác vụ mã hóa/giải mã video cơ bản, dù không mạnh mẽ như JavaCV nhưng đơn giản hơn.
3.	DatagramSocket API của Java: Để gửi và nhận gói UDP.
