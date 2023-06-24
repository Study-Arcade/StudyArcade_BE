package trillion9.studyarcade_be.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import trillion9.studyarcade_be.member.MemberRepository;

@RequiredArgsConstructor
@Service
public class ChatService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic channelTopic;
	private final MemberRepository memberRepository;

	public void message(ChatMessageDto message) {

		// Websocket에 발행된 메시지를 redis로 발행 (publish)
		redisTemplate.convertAndSend(channelTopic.getTopic(), message);
	}

}