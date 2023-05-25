package trillion9.studyarcade_be.member;

import static trillion9.studyarcade_be.global.exception.ErrorCode.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.global.jwt.JwtUtil;
import trillion9.studyarcade_be.global.jwt.RefreshToken;
import trillion9.studyarcade_be.global.jwt.RefreshTokenRepository;
import trillion9.studyarcade_be.global.jwt.TokenDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate redisTemplate;

    @Transactional
    public ResponseDto<String> register(MemberRequestDto memberRequestDto) {
        return ResponseDto.setSuccess("임시");
    }

    @Transactional
    public ResponseDto<String> login(final MemberRequestDto.login loginRequestDto, final HttpServletResponse response) {

        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        // 멤버 조회
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(INVALID_USER_PASSWORD);
        }

        // Token 생성
        TokenDto tokenDto = jwtUtil.createAllToken(member.getEmail());

        //redis에 RT:13@gmail.com(key) / 23jijiofj2io3hi32hiongiodsninioda(value) 형태로 리프레시 토큰 저장하기
        redisTemplate.opsForValue().set("RT:" + member.getEmail(), tokenDto.getRefreshToken(), JwtUtil.REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);

        // Header에 accesstoken, refreshtoken 추가
        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());

        return ResponseDto.setSuccess("로그인 성공");
    }

    @Transactional
    public ResponseDto<String> logout(HttpServletRequest request, Member member) {
        String accessToken = jwtUtil.resolveToken(request, JwtUtil.ACCESS_TOKEN);
        // 로그아웃 하고 싶은 토큰이 유효한 지 먼저 검증하기
        if (!jwtUtil.validateToken(accessToken)) {
            throw new CustomException(INVALID_TOKEN);
        }

        // Redis에서 해당 User email로 저장된 Refresh Token 이 있는지 여부를 확인 후에 있을 경우 삭제를 한다.
        if (redisTemplate.opsForValue().get("RT:" + member.getEmail()) != null) {
            // Refresh Token을 삭제
            redisTemplate.delete("RT:" + member.getEmail());
        }

        // 해당 Access Token 유효시간을 가지고 와서 BlackList에 저장하기
        redisTemplate.opsForValue().set("BL:" + accessToken, "", jwtUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);

        return ResponseDto.setSuccess("로그아웃 성공");
    }
}