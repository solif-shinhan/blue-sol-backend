package com.solif.backend.global.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.solif.backend.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final S3Service s3Service;

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    public record QrResult(String qrData, String imageUrl) {}

    /**
     * QR 코드 생성 및 S3 업로드
     * @param userId 사용자 ID
     * @return QR 코드 고유 데이터 + 이미지 URL
     */
    public QrResult generateAndUpload(Long userId) {
        // 1. QR 코드에 담길 고유 데이터 생성
        String qrData = "SOL-" + UUID.randomUUID().toString();

        // 2. QR 코드 이미지 생성
        byte[] qrImage = generateQrImage(qrData);

        // 3. S3에 업로드
        String fileName = "user_" + userId + "_" + System.currentTimeMillis() + ".png";
        String imageUrl = s3Service.upload("qrcodes", fileName, qrImage, "image/png");

        log.info("QR 코드 생성 완료 - userId: {}, data: {}, url: {}", userId, qrData, imageUrl);

        return new QrResult(qrData, imageUrl);
    }

    /**
     * QR 코드 이미지 바이트 배열 생성
     */
    private byte[] generateQrImage(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }
    }
}
