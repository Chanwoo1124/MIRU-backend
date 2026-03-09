package com.miru.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NicknameUpdateRequestDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 15, message = "닉네임은 15자 이하로 입력해주세요.")
    private String nickname;
}
