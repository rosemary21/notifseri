package com.creditville.notifications.services.impl;

import com.creditville.notifications.models.CardTransactions;
import com.creditville.notifications.models.DTOs.RetryLoanRepaymentDTO;
import com.creditville.notifications.models.Mandates;
import com.creditville.notifications.models.RetryLoanRepayment;
import com.creditville.notifications.repositories.MandateRepository;
import com.creditville.notifications.repositories.RetryLoanRepaymentRepository;
import com.creditville.notifications.services.RetryLoanRepaymentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RetryLoanRepaymentServiceImpl implements RetryLoanRepaymentService {


    @Autowired
    ModelMapper modelMapper;

    @Autowired
    RetryLoanRepaymentRepository loanRepaymentRepository;

    @Autowired
    MandateRepository  mandateRepository;

    @Value("${app.no.retry}")
    private String noRetry;

    @Override
    public String saveRetryLoan(CardTransactions cardTransactions,RetryLoanRepaymentDTO retryLoanRepaymentDTO,String loanId) {
        RetryLoanRepayment loanRepayment= modelMapper.map(retryLoanRepaymentDTO,RetryLoanRepayment.class);
            RetryLoanRepayment oldrepayment= loanRepaymentRepository.findByReferenceAndLoanIdAndClientId(cardTransactions.getReference(),loanId,retryLoanRepaymentDTO.getClientId());
            if(oldrepayment==null){
                loanRepaymentRepository.save(loanRepayment);
                return "success";
            }

        return null;
    }

    @Override
    public RetryLoanRepaymentDTO getLoanRepayment(CardTransactions cardTransactions,String loanId,String email,String instafinOblDate) {

        RetryLoanRepaymentDTO retryLoanRepaymentDTO=new RetryLoanRepaymentDTO();
        System.out.println("getting the reference and loan id {}{}{}"+cardTransactions.getReference() +" "+loanId+" "+cardTransactions.getCardDetails().getClientId());
        RetryLoanRepayment loanRepayment= loanRepaymentRepository.findByReferenceAndLoanIdAndClientId(cardTransactions.getReference(),loanId,cardTransactions.getCardDetails().getClientId());
        if(loanRepayment==null){
            retryLoanRepaymentDTO.setLoanId(loanId);
            retryLoanRepaymentDTO.setNoOfRetry(0);
            retryLoanRepaymentDTO.setStatus(cardTransactions.getStatus());
            retryLoanRepaymentDTO.setClientId(cardTransactions.getCardDetails().getClientId());
            retryLoanRepaymentDTO.setTransactionDate(cardTransactions.getTransactionDate());
            retryLoanRepaymentDTO.setAmount(cardTransactions.getAmount());
            retryLoanRepaymentDTO.setReference(cardTransactions.getReference());
            retryLoanRepaymentDTO.setEmail(email);
            retryLoanRepaymentDTO.setProcessFlag("N");
            retryLoanRepaymentDTO.setInstafinObliDate(instafinOblDate);

        }
        return retryLoanRepaymentDTO;
    }

    @Override
    public RetryLoanRepaymentDTO getLoanMandateRepayment(CardTransactions cardTransactions,String loanId,String mandateid,String instafinOblDate) {

        RetryLoanRepaymentDTO retryLoanRepaymentDTO=new RetryLoanRepaymentDTO();
        RetryLoanRepayment loanRepayment=loanRepaymentRepository.findByReferenceAndMandateId(cardTransactions.getReference(),mandateid);
        if(mandateid!=null){
          Mandates mandates= mandateRepository.findByMandateId(mandateid);
            retryLoanRepaymentDTO.setClientId(mandates.getClientId());

        }
        if(cardTransactions.getCardDetails()!=null){
            retryLoanRepaymentDTO.setClientId(cardTransactions.getCardDetails().getClientId());
        }
        if(loanRepayment==null){
            retryLoanRepaymentDTO.setLoanId(loanId);
            retryLoanRepaymentDTO.setNoOfRetry(0);
            retryLoanRepaymentDTO.setStatus(cardTransactions.getStatus());
            retryLoanRepaymentDTO.setTransactionDate(cardTransactions.getTransactionDate());
            retryLoanRepaymentDTO.setAmount(cardTransactions.getAmount());
            retryLoanRepaymentDTO.setReference(cardTransactions.getReference());
            retryLoanRepaymentDTO.setMandateId(mandateid);
            retryLoanRepaymentDTO.setProcessFlag("N");
            retryLoanRepaymentDTO.setInstafinObliDate(instafinOblDate);
        }
        return retryLoanRepaymentDTO;
    }

    @Override
    public String updateRetryLoan(RetryLoanRepayment retryLoanRepayment) {
        RetryLoanRepayment oldrepayment= loanRepaymentRepository.findByReferenceAndLoanIdAndClientId(retryLoanRepayment.getReference(),retryLoanRepayment.getLoanId(),retryLoanRepayment.getClientId());
        if(oldrepayment!=null){
               if(oldrepayment.getNoOfRetry()<Integer.valueOf(noRetry)){
                oldrepayment.setNoOfRetry(oldrepayment.getNoOfRetry()+1);
                loanRepaymentRepository.save(oldrepayment);
                return "success";
              }
        else{
            oldrepayment.setManualStatus("Y");
            loanRepaymentRepository.save(oldrepayment);
            return "success";
        }
    }
    return null;
}


    @Override
    public String repaymentOfLoan(RetryLoanRepayment retryLoanRepayment) {
        RetryLoanRepayment oldrepayment= loanRepaymentRepository.findByReferenceAndLoanIdAndClientId(retryLoanRepayment.getReference(),retryLoanRepayment.getLoanId(),retryLoanRepayment.getClientId());
        if(oldrepayment!=null){
            if(oldrepayment.getNoOfRetry()<Integer.valueOf(noRetry)){
                oldrepayment.setNoOfRetry(oldrepayment.getNoOfRetry()+1);
                oldrepayment.setProcessFlag("Y");
                oldrepayment.setStatus("Successfully_Repaid");
                loanRepaymentRepository.save(oldrepayment);
                return "success";
            }

        }
        return null;
    }



}
