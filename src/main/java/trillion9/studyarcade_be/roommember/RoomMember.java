package trillion9.studyarcade_be.roommember;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import trillion9.studyarcade_be.member.Member;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class RoomMember {
    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ColumnDefault("false")
    private boolean roomMaster;

    public RoomMember(Member member) {
        this.member = member;
    }

    public void setRoomMaster(boolean roomMaster) {
        this.roomMaster = roomMaster;
    }
}
