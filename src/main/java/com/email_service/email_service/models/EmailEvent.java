package com.email_service.email_service.models;

public record EmailEvent(String to, String subject, String body) {}
