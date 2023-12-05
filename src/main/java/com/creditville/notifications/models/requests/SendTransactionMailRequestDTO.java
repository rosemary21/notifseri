package com.creditville.notifications.models.requests;

import com.creditville.notifications.models.DTOs.TransactionDTO;
import com.creditville.notifications.models.To;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTransactionMailRequestDTO {
    @NotNull(message = "To is a required field")
    private List<To> tos;
    private List<To> bcc;
    private List<To> cc;
    @NotEmpty(message = "From name is a required field")
    private String fromName;
    @NotEmpty(message = "from Email is a required field")
    private String fromEmail;
    @NotEmpty(message = "Subject is a required field")
    private String subject;
    private TransactionDTO transactionDTO;
}
