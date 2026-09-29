package com.huellitas.vetturno.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(int status, String mensaje,
                       Map<String, String> errores, LocalDateTime timestamp) {}