package com.footballacademy.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ApiError> errors;
    private LocalDateTime timestamp;
    private PaginationMetadata pagination;
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    public ApiResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    public ApiResponse(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }
    public static <T> ApiResponse<T> error(String message, List<ApiError> errors) {
        ApiResponse<T> response = new ApiResponse<>(false, message);
        response.setErrors(errors);
        return response;
    }
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public List<ApiError> getErrors() {
        return errors;
    }
    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public PaginationMetadata getPagination() {
        return pagination;
    }
    public void setPagination(PaginationMetadata pagination) {
        this.pagination = pagination;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static
    class PaginationMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        public PaginationMetadata(int page, int size, long totalElements, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = page == 0;
            this.last = page >= totalPages - 1;
        }
        // Getters and Setters
        public int getPage() {
            return page;
        }
        public void setPage(int page) {
            this.page = page;
        }
        public int getSize() {
            return size;
        }
        public void setSize(int size) {
            this.size = size;
        }
        public long getTotalElements() {
            return totalElements;
        }
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        public int getTotalPages() {
            return totalPages;
        }
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        public boolean isFirst() {
            return first;
        }
        public void setFirst(boolean first) {
            this.first = first;
        }
        public boolean isLast() {
            return last;
        }
        public void setLast(boolean last) {
            this.last = last;
        }
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static
    class ApiError {
        private String field;
        private String message;
        private String code;
        public ApiError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        public ApiError(String field, String message, String code) {
            this.field = field;
            this.message = message;
            this.code = code;
        }
        // Getters and Setters
        public String getField() {
            return field;
        }
        public void setField(String field) {
            this.field = field;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
    }
}
