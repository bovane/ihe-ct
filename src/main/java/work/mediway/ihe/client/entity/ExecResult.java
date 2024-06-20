package work.mediway.ihe.client.entity;

import lombok.*;

/**
 * @author BoVane
 * @version 1.0
 * @description: TODO
 * @date 2023/6/5 20:15
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ExecResult {
    private boolean success;
    private String result;
}
