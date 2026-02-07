package com.miru.domain.agreements.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AgreementType {
    TERMS_OF_SERVICE("이용약관"),
    PRIVACY_POLICY("개인정보 처리방침");

    private final String description;
}