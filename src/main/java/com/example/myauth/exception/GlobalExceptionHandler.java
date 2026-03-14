package com.example.myauth.exception;

import com.example.myauth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * ?꾩뿭 ?덉쇅 泥섎━ ?몃뱾??
 * 紐⑤뱺 @RestController?먯꽌 諛쒖깮?섎뒗 ?덉쇅瑜???怨녹뿉??泥섎━?쒕떎
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * ?몄쬆 ?ㅽ뙣 ?덉쇅 泥섎━
   * ?대찓???먮뒗 鍮꾨?踰덊샇媛 ?щ컮瑜댁? ?딆쓣 ??諛쒖깮
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
      InvalidCredentialsException ex) {
    log.warn("?몄쬆 ?ㅽ뙣: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?좏겙 愿???덉쇅 泥섎━
   * Refresh Token???좏슚?섏? ?딄굅??留뚮즺?섏뿀????諛쒖깮
   */
  @ExceptionHandler(TokenException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleTokenException(
      TokenException ex) {
    log.warn("?좏겙 ?ㅻ쪟: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 怨꾩젙 ?곹깭 愿???덉쇅 泥섎━
   * 怨꾩젙??鍮꾪솢?깊솕, ?뺤?, ??젣 ?깆쓽 ?곹깭????諛쒖깮
   */
  @ExceptionHandler(AccountException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleAccountException(
      AccountException ex) {
    log.warn("怨꾩젙 ?곹깭 ?ㅻ쪟: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?대찓??以묐났 ?덉쇅 泥섎━
   * ?뚯썝媛?????대? 議댁옱?섎뒗 ?대찓?쇰줈 媛?낆쓣 ?쒕룄????諛쒖깮
   */
  @ExceptionHandler(DuplicateEmailException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(
      DuplicateEmailException ex) {
    log.warn("?대찓??以묐났: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?뚯씪 ?????젣 ?ㅽ뙣 ?덉쇅 泥섎━
   * ?뚯씪 ?쒖뒪??愿??IO ?ㅻ쪟 諛쒖깮 ??
   */
  @ExceptionHandler(FileStorageException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleFileStorageException(
      FileStorageException ex) {
    log.error("?뚯씪 ????ㅻ쪟: {}", ex.getMessage(), ex);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?뚯씪 ?좏슚??寃???ㅽ뙣 ?덉쇅 泥섎━
   * ?뚯씪??鍮꾩뼱?덇굅?? ?ш린 珥덇낵, 吏?먰븯吏 ?딅뒗 ?뺤떇 ??
   */
  @ExceptionHandler(InvalidFileException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleInvalidFileException(
      InvalidFileException ex) {
    log.warn("?뚯씪 ?좏슚??寃???ㅽ뙣 [{}]: {}", ex.getErrorCode(), ex.getMessage());

    // ?먮윭 肄붾뱶???곕Ⅸ HTTP ?곹깭 肄붾뱶 寃곗젙
    HttpStatus status = switch (ex.getErrorCode()) {
      case EMPTY_FILE, FILE_TOO_LARGE, UNSUPPORTED_TYPE, INVALID_FILENAME -> HttpStatus.BAD_REQUEST;
      case INVALID_PATH -> HttpStatus.FORBIDDEN;  // 寃쎈줈 議곗옉 ?쒕룄??403
    };

    return ResponseEntity
        .status(status)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 寃뚯떆湲??李얠쓣 ???놁쓣 ???덉쇅 泥섎━
   * 議댁옱?섏? ?딅뒗 寃뚯떆湲 ID濡?議고쉶/?섏젙/??젣 ?쒕룄 ??諛쒖깮
   */
  @ExceptionHandler(PostNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handlePostNotFoundException(
      PostNotFoundException ex) {
    log.warn("寃뚯떆湲 議고쉶 ?ㅽ뙣: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 沅뚰븳 ?녿뒗 ?묎렐 ?덉쇅 泥섎━
   * ?ㅻⅨ ?ъ슜?먯쓽 寃뚯떆湲 ?섏젙/??젣 ?쒕룄 ?먮뒗 鍮꾧났媛?寃뚯떆湲 ?묎렐 ??諛쒖깮
   */
  @ExceptionHandler(UnauthorizedAccessException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccessException(
      UnauthorizedAccessException ex) {
    log.warn("沅뚰븳 ?녿뒗 ?묎렐 ?쒕룄: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(AdminAccessDeniedException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleAdminAccessDeniedException(
      AdminAccessDeniedException ex) {
    log.warn("Admin access denied: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?볤???李얠쓣 ???놁쓣 ???덉쇅 泥섎━
   * 議댁옱?섏? ?딅뒗 ?볤? ID濡?議고쉶/?섏젙/??젣 ?쒕룄 ??諛쒖깮
   */
  @ExceptionHandler(CommentNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleCommentNotFoundException(
      CommentNotFoundException ex) {
    log.warn("?볤? 議고쉶 ?ㅽ뙣: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 以묐났 醫뗭븘???덉쇅 泥섎━
   * ?대? 醫뗭븘?뷀븳 寃뚯떆湲/?볤????ㅼ떆 醫뗭븘???쒕룄 ??諛쒖깮
   */
  @ExceptionHandler(DuplicateLikeException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDuplicateLikeException(
      DuplicateLikeException ex) {
    log.warn("以묐났 醫뗭븘???쒕룄: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 醫뗭븘??湲곕줉 ?놁쓬 ?덉쇅 泥섎━
   * 醫뗭븘?뷀븯吏 ?딆? 寃뚯떆湲/?볤???醫뗭븘??痍⑥냼 ?쒕룄 ??諛쒖깮
   */
  @ExceptionHandler(LikeNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleLikeNotFoundException(
      LikeNotFoundException ex) {
    log.warn("醫뗭븘??湲곕줉 ?놁쓬: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?섎せ???몄옄 ?덉쇅 泥섎━
   * 鍮꾩쫰?덉뒪 濡쒖쭅?먯꽌 ?좏슚?섏? ?딆? ?몄옄 ?꾨떖 ??諛쒖깮
   * ?? ??볤?????볤? ?묒꽦 ?쒕룄
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.warn("?섎せ???붿껌: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?ъ슜?먮? 李얠쓣 ???놁쓣 ???덉쇅 泥섎━
   */
  @ExceptionHandler(UserNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
      UserNotFoundException ex) {
    log.warn("?ъ슜??議고쉶 ?ㅽ뙣: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?먭린 ?먯떊 ?붾줈???쒕룄 ?덉쇅 泥섎━
   */
  @ExceptionHandler(SelfFollowException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleSelfFollowException(
      SelfFollowException ex) {
    log.warn("?먭린 ?붾줈???쒕룄: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 以묐났 ?붾줈???덉쇅 泥섎━
   */
  @ExceptionHandler(DuplicateFollowException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDuplicateFollowException(
      DuplicateFollowException ex) {
    log.warn("以묐났 ?붾줈???쒕룄: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?붾줈??愿怨??놁쓬 ?덉쇅 泥섎━
   */
  @ExceptionHandler(FollowNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleFollowNotFoundException(
      FollowNotFoundException ex) {
    log.warn("?붾줈??愿怨??놁쓬: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 以묐났 遺곷쭏???덉쇅 泥섎━
   */
  @ExceptionHandler(DuplicateBookmarkException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDuplicateBookmarkException(
      DuplicateBookmarkException ex) {
    log.warn("以묐났 遺곷쭏???쒕룄: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 遺곷쭏???놁쓬 ?덉쇅 泥섎━
   */
  @ExceptionHandler(BookmarkNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleBookmarkNotFoundException(
      BookmarkNotFoundException ex) {
    log.warn("遺곷쭏???놁쓬: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * ?댁떆?쒓렇 ?놁쓬 ?덉쇅 泥섎━
   */
  @ExceptionHandler(HashtagNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleHashtagNotFoundException(
      HashtagNotFoundException ex) {
    log.warn("?댁떆?쒓렇 ?놁쓬: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * Bean Validation 寃利??ㅽ뙣 ??泥섎━
   * Controller?먯꽌 @Valid ?대끂?뚯씠?섏쑝濡?寃利??ㅽ뙣??寃쎌슦 諛쒖깮?섎뒗 ?덉쇅瑜?泥섎━?쒕떎
   *
   * @param ex 寃利??ㅽ뙣 ?덉쇅 媛앹껜
   * @return 泥?踰덉㎏ ?먮윭 硫붿떆吏瑜??ы븿??ApiResponse
   */
  /**
   * DM 방 없음 예외 처리
   */
  @ExceptionHandler(DmRoomNotFoundException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDmRoomNotFoundException(
      DmRoomNotFoundException ex) {
    log.warn("DM 방 조회 실패: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * DM 접근 거부 예외 처리
   */
  @ExceptionHandler(DmAccessDeniedException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDmAccessDeniedException(
      DmAccessDeniedException ex) {
    log.warn("DM 접근 거부: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * DM 정책 위반 예외 처리
   */
  @ExceptionHandler(DmPolicyViolationException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDmPolicyViolationException(
      DmPolicyViolationException ex) {
    log.warn("DM 정책 위반: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * DM 메시지 검증 실패 예외 처리
   */
  @ExceptionHandler(DmMessageValidationException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleDmMessageValidationException(
      DmMessageValidationException ex) {
    log.warn("DM 메시지 검증 실패: {}", ex.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @SuppressWarnings("NullableProblems")  // ApiResponse????긽 non-null??諛섑솚?섎?濡?寃쎄퀬 ?듭젣
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    // 泥?踰덉㎏ ?먮윭 硫붿떆吏留?諛섑솚
    String errorMessage = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .findFirst()
        .map(ObjectError::getDefaultMessage)
        .orElse("?낅젰媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎.");

    log.warn("?낅젰媛?寃利??ㅽ뙣: {}", errorMessage);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(errorMessage));
  }

  /**
   * HTTP ?붿껌 body ?쎄린 ?ㅽ뙣 ??泥섎━
   * - ?붿껌 body媛 鍮꾩뼱?덇굅???꾩닔?몃뜲 ?녿뒗 寃쎌슦
   * - JSON ?뚯떛 ?ㅽ뙣 (?섎せ??JSON ?뺤떇)
   * - Content-Type怨??ㅼ젣 body ?댁슜???쇱튂?섏? ?딅뒗 寃쎌슦
   *
   * @param ex HttpMessageNotReadableException
   * @return ?ъ슜??移쒗솕?곸씤 ?먮윭 硫붿떆吏
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {

    String errorMessage;
    String detailMessage = ex.getMessage();

    // ?먮윭 硫붿떆吏 遺꾩꽍?섏뿬 ?ъ슜??移쒗솕?곸씤 硫붿떆吏 ?앹꽦
    if (detailMessage != null && detailMessage.contains("Required request body is missing")) {
      errorMessage = "?붿껌 body媛 鍮꾩뼱?덉뒿?덈떎. JSON ?뺤떇???곗씠?곕? ?꾩넚?댁＜?몄슂.";
    } else {
      errorMessage = "?섎せ???붿껌 ?뺤떇?낅땲?? JSON ?뺤떇???щ컮瑜몄? ?뺤씤?댁＜?몄슂.";
    }

    log.warn("HTTP 硫붿떆吏 ?쎄린 ?ㅽ뙣: {}", errorMessage);
    log.debug("?곸꽭 ?먮윭: {}", detailMessage);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(errorMessage));
  }

  /**
   * 吏?먰븯吏 ?딅뒗 Content-Type?쇰줈 ?붿껌??寃쎌슦 泥섎━
   * ?? application/json??湲곕??섎뒗??application/x-www-form-urlencoded濡??붿껌
   *
   * @param ex HttpMediaTypeNotSupportedException
   * @return ?ъ슜??移쒗솕?곸씤 ?먮윭 硫붿떆吏
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex) {

    String errorMessage = String.format(
        "吏?먰븯吏 ?딅뒗 Content-Type?낅땲?? 'Content-Type: application/json' ?ㅻ뜑瑜?異붽??댁＜?몄슂.",
        ex.getContentType()
    );

    log.warn("吏?먰븯吏 ?딅뒗 Media Type: {}", ex.getContentType());

    return ResponseEntity
        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(ApiResponse.error(errorMessage));
  }

  /**
   * 吏?먰븯吏 ?딅뒗 HTTP 硫붿꽌?쒕줈 ?붿껌??寃쎌슦 泥섎━
   * ?? POST留?吏?먰븯???붾뱶?ъ씤?몄뿉 GET ?붿껌
   *
   * @param ex HttpRequestMethodNotSupportedException
   * @return ?ъ슜??移쒗솕?곸씤 ?먮윭 硫붿떆吏
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {

    String supportedMethods = ex.getSupportedHttpMethods() != null
        ? ex.getSupportedHttpMethods().toString()
        : "吏?먮릺??硫붿꽌???놁쓬";

    String errorMessage = String.format(
        "%s 硫붿꽌?쒕뒗 吏?먰븯吏 ?딆뒿?덈떎. 吏?먰븯??硫붿꽌?? %s",
        ex.getMethod(),
        supportedMethods
    );

    log.warn("吏?먰븯吏 ?딅뒗 HTTP 硫붿꽌?? {} (?붿껌??硫붿꽌??, 吏?? {}",
        ex.getMethod(), supportedMethods);

    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ApiResponse.error(errorMessage));
  }

  /**
   * ?몃옖??뀡 濡ㅻ갚 ?덉쇅 泥섎━
   * @Transactional 硫붿꽌?쒖뿉???덉쇅瑜?catch?댁꽌 泥섎━?덉?留??몃옖??뀡??rollback-only濡?留덊궧??寃쎌슦 諛쒖깮
   *
   * @param ex UnexpectedRollbackException
   * @return ?곸젅???먮윭 硫붿떆吏
   */
  @ExceptionHandler(UnexpectedRollbackException.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedRollbackException(
      UnexpectedRollbackException ex) {

    log.warn("?몃옖??뀡 濡ㅻ갚 ?덉쇅 諛쒖깮: {}", ex.getMessage());

    // ?먯씤 ?덉쇅瑜??ш??곸쑝濡??먯깋
    Throwable cause = ex;
    while (cause != null) {
      log.debug("?덉쇅 泥댁씤: {}", cause.getClass().getName());

      // DataIntegrityViolationException 諛쒓껄
      if (cause instanceof DataIntegrityViolationException) {
        log.warn("?곗씠??臾닿껐???쒖빟 ?꾨컲 諛쒓껄: {}", cause.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("?대? 媛?낅맂 ?대찓?쇱엯?덈떎."));
      }

      cause = cause.getCause();
    }

    // DataIntegrityViolationException??李얠? 紐삵븳 寃쎌슦
    // (?뚯썝媛????以묐났 ?대찓???먮윭媛 ?遺遺꾩씠誘濡?湲곕낯 硫붿떆吏 ?쒓났)
    log.warn("원인 예외를 특정하지 못했지만 중복 이메일로 추정합니다.");
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("?대? 媛?낅맂 ?대찓?쇱엯?덈떎."));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
      NoResourceFoundException ex
  ) {
    log.error("No Resource Found Exception: "+ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 紐⑤뱺 ?덉쇅瑜?泥섎━?섎뒗 理쒗썑??諛⑹뼱??
   * ?ㅻⅨ ExceptionHandler?먯꽌 泥섎━?섏? ?딆? 紐⑤뱺 ?덉쇅瑜??ш린??泥섎━?쒕떎
   *
   * @param ex 諛쒖깮???덉쇅 媛앹껜
   * @return ?쒕쾭 ?ㅻ쪟 硫붿떆吏瑜??ы븿??ApiResponse
   */
  @ExceptionHandler(Exception.class)
  @SuppressWarnings("NullableProblems")
  public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
    // ?ㅽ깮 ?몃젅?댁뒪?먯꽌 ?덉쇅 諛쒖깮 ?꾩튂 異붿텧
    StackTraceElement[] stackTrace = ex.getStackTrace();
    String errorLocation = "?????놁쓬";

    if (stackTrace != null && stackTrace.length > 0) {
      StackTraceElement firstElement = stackTrace[0];
      errorLocation = String.format("%s.%s (line: %d)",
          firstElement.getClassName(),
          firstElement.getMethodName(),
          firstElement.getLineNumber());
    }

    // ?곸꽭??濡쒓렇 湲곕줉 (媛쒕컻?먯슜)
    log.error("=== ?덉긽移?紐삵븳 ?ㅻ쪟 諛쒖깮 ===");
    log.error("?덉쇅 ??? {}", ex.getClass().getName());
    log.error("?덉쇅 硫붿떆吏: {}", ex.getMessage());
    log.error("諛쒖깮 ?꾩튂: {}", errorLocation);
    log.error("?꾩껜 ?ㅽ깮 ?몃젅?댁뒪:", ex);
    log.error("===========================");

    // ?대씪?댁뼵?몄뿉寃뚮뒗 媛꾨떒??硫붿떆吏留?諛섑솚 (蹂댁븞???곸꽭 ?뺣낫???④?)
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("?쒕쾭 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. ?좎떆 ???ㅼ떆 ?쒕룄?댁＜?몄슂."));
  }
}



