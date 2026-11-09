package com.example.astrology.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ControllerAdvice chuyên trách xử lý ngoại lệ tập trung toàn hệ thống.
 * Phân định rõ ràng:
 * - Lỗi dự kiến (input không hợp lệ, ràng buộc nghiệp vụ): ghi nhận mức WARN và trả về thông điệp thân thiện.
 * - Lỗi hệ thống (CSDL, kết nối dịch vụ, lỗi không mong đợi): ghi nhận mức ERROR kèm stack trace và che giấu chi tiết nhạy cảm.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResponseStatusException.class)
	public void handleResponseStatus(ResponseStatusException ex) {
		// Cho phép Spring MVC xử lý trực tiếp HTTP status code (404 Not Found, 403 Forbidden, v.v.)
		throw ex;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
		logger.warn("Dữ liệu đầu vào hoặc tham số không hợp lệ tại {}: {}", request.getRequestURI(), ex.getMessage());
		model.addAttribute("errorMessage", ex.getMessage());
		return "home";
	}

	@ExceptionHandler(DataAccessException.class)
	public String handleDatabaseException(DataAccessException ex, HttpServletRequest request, Model model) {
		logger.error("Lỗi truy xuất cơ sở dữ liệu khi xử lý {}: {}", request.getRequestURI(), ex.getMessage(), ex);
		model.addAttribute("errorMessage", "Không thể kết nối hoặc truy xuất dữ liệu từ hệ thống. Vui lòng thử lại sau.");
		return "home";
	}

	@ExceptionHandler(Exception.class)
	public String handleUnhandledException(Exception ex, HttpServletRequest request, Model model) {
		logger.error("Lỗi hệ thống chưa được kiểm soát tại {}: {}", request.getRequestURI(), ex.getMessage(), ex);
		model.addAttribute("errorMessage", "Đã xảy ra sự cố kỹ thuật không mong muốn. Đội ngũ kỹ thuật đã được ghi nhận.");
		return "home";
	}
}
