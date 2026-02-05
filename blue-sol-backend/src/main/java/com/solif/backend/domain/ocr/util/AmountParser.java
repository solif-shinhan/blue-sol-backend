package com.solif.backend.domain.ocr.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AmountParser {

    private static final Pattern AMOUNT_PATTERN_WITH_COMMA = Pattern.compile("\\d{1,3}(,\\d{3})+");
    private static final Pattern AMOUNT_PATTERN_WITH_WON = Pattern.compile("(\\d+)원");

    /**
     * OCR 텍스트에서 금액 후보들을 추출하고, 가장 큰 값을 반환
     * @param ocrText OCR로 추출된 텍스트
     * @return 가장 큰 금액 (없으면 null)
     */
    public static Long extractAmount(String ocrText) {
        List<Long> candidates = extractAllAmounts(ocrText);

        if (candidates.isEmpty()) {
            log.warn("금액을 찾을 수 없습니다. OCR Text: {}", ocrText);
            return null;
        }

        Long maxAmount = candidates.stream()
                .max(Long::compareTo)
                .orElse(null);

        log.info("추출된 금액 후보: {}, 최종 금액: {}", candidates, maxAmount);
        return maxAmount;
    }

    /**
     * OCR 텍스트에서 모든 금액 후보들을 추출
     * @param ocrText OCR로 추출된 텍스트
     * @return 추출된 모든 금액 리스트
     */
    public static List<Long> extractAllAmounts(String ocrText) {
        List<Long> amounts = new ArrayList<>();

        if (ocrText == null || ocrText.isBlank()) {
            return amounts;
        }

        // 패턴 1: 쉼표 포함 숫자 (예: "13,200")
        Matcher commaMatcher = AMOUNT_PATTERN_WITH_COMMA.matcher(ocrText);
        while (commaMatcher.find()) {  // ✅ find()로 수정
            String match = commaMatcher.group();
            try {
                Long amount = parseAmount(match);
                amounts.add(amount);
            } catch (NumberFormatException e) {
                log.debug("금액 파싱 실패: {}", match);
            }
        }

        // 패턴 2: "원" 포함 숫자 (예: "13200원")
        Matcher wonMatcher = AMOUNT_PATTERN_WITH_WON.matcher(ocrText);
        while (wonMatcher.find()) {
            String match = wonMatcher.group(1);
            try {
                Long amount = parseAmount(match);
                amounts.add(amount);
            } catch (NumberFormatException e) {
                log.debug("금액 파싱 실패: {}", match);
            }
        }

        return amounts.stream()
                .distinct()
                .sorted()
                .toList();
    }

    // 쉼표를 제거하고 숫자로 변환
    private static Long parseAmount(String amountStr) {
        String cleaned = amountStr.replaceAll(",", "");
        return Long.parseLong(cleaned);
    }
}