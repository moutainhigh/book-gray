package cn.dingyuegroup.gray.server.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * Created by 170147 on 2019/1/30.
 */
@Data
@AllArgsConstructor
public class GrantedAuthorityVO implements GrantedAuthority {

    private String name;

    @Override
    public String getAuthority() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
