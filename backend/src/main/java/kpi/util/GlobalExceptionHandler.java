package kpi.util;

import kpi.exception.DuplicateFilmException;
import kpi.exception.NoFreePlaceException;
import kpi.exception.NotEnoughMoneyException;
import kpi.security.JwtTokenRepository;
import lombok.Getter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private JwtTokenRepository tokenRepository;

    public GlobalExceptionHandler(JwtTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @ExceptionHandler({AuthenticationException.class, MissingCsrfTokenException.class, InvalidCsrfTokenException.class,
            SessionAuthenticationException.class})
    public ErrorInfo handleAuthenticationException(RuntimeException ex, HttpServletRequest request,
                                                   HttpServletResponse response){
        this.tokenRepository.clearToken(response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ErrorInfo(UrlUtils.buildFullRequestUrl(request), "Invalid password or email");
    }

    @ExceptionHandler({NoFreePlaceException.class})
    public ErrorInfo handleNoFreePlaceException(RuntimeException ex, HttpServletRequest request,
                                                   HttpServletResponse response){
        return new ErrorInfo(UrlUtils.buildFullRequestUrl(request), "No free places for this movie-session");
    }

    @ExceptionHandler({NotEnoughMoneyException.class})
    public ErrorInfo handleNotEnoughMoneyException(RuntimeException ex, HttpServletRequest request,
                                                HttpServletResponse response){
        return new ErrorInfo(UrlUtils.buildFullRequestUrl(request), "Not enough money to but a ticket");
    }

    @ExceptionHandler({DuplicateFilmException.class})
    public ErrorInfo handleDuplicateFilmExceptionException(RuntimeException ex, HttpServletRequest request,
                                                   HttpServletResponse response){
        return new ErrorInfo(UrlUtils.buildFullRequestUrl(request), "This film is already in database");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatus status,
                                                                  final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = getErrors(ex);
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    private List<String> getErrors(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return errors;
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers,
                                                         final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers,
                                                        final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = ex.getValue() + " value for " + ex.getPropertyName() + " should be of type " + ex.getRequiredType();

        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex,
                                                                     final HttpHeaders headers, final HttpStatus status,
                                                                     final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = ex.getRequestPartName() + " part is missing";
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex,
                                                                          final HttpHeaders headers, final HttpStatus status,
                                                                          final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = ex.getParameterName() + " parameter is missing";
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex,
                                                                   final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({WebExchangeBindException.class})
    public ResponseEntity<Object> handleWebExchangeBindException(final WebExchangeBindException ex,
                                                                 final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = "Invalid value";
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex,
                                                            final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = new ArrayList<>();
        for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " + violation.getPropertyPath() + ": "
                    + violation.getMessage());
        }
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex,
                                                                   final HttpHeaders headers, final HttpStatus status,
                                                                   final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(final HttpRequestMethodNotSupportedException ex,
                                                                         final HttpHeaders headers, final HttpStatus status,
                                                                         final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));
        final ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage(), builder.toString());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex,
                                                                     final HttpHeaders headers, final HttpStatus status,
                                                                     final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));

        final ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getLocalizedMessage(),
                builder.substring(0, builder.length() - 2));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

//    @ExceptionHandler({ Exception.class })
//    public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request) {
//        logger.info(ex.getClass().getName());
//        logger.error("error", ex);
//        final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
//        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
//    }

    @Getter
    public class ErrorInfo {
        private final String url;
        private final String info;

        ErrorInfo(String url, String info) {
            this.url = url;
            this.info = info;
        }
    }
}