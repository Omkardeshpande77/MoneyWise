package in.omkar.moneywise.service;

import in.omkar.moneywise.dto.ExpenseDTO;
import in.omkar.moneywise.entity.ProfileEntity;
import in.omkar.moneywise.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {


    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.wise.frontend.url}")
    private String frontendUrl;

    //    @Scheduled(cron = "0 * * * * *",zone = "IST")
    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job Started : sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            String to = profile.getEmail();
            String subject = "Daily Income and Expense Reminder";
            String body = "Dear " + profile.getName() + ",\n\n" +
                    "This is a friendly reminder to log your daily income and expenses. Keeping track of your finances is crucial for effective budgeting and financial planning.\n\n" +
                    "You can log your income and expenses by visiting the following link:\n" +
                    frontendUrl + "/dashboard\n\n" +
                    "Thank you for using MoneyWise!\n\n" +
                    "Best regards,\n" +
                    "The MoneyWise Team";
            try {
                emailService.sendEmail(to, subject, body);
                log.info("Reminder email sent to: {}", to);
            } catch (Exception e) {
                log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
            }
        }
        log.info("Job Ended : sendDailyIncomeExpenseReminder()");
    }

//    @Scheduled(cron = "0 * * * * *", zone = "IST")
    @Scheduled(cron = "0 0 21 * * *",zone = "IST")
    public void sendDailyExpenseSummary() {
        log.info("Job Started : sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> todaysExpense = expenseService.getExpenseForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));
            if (!todaysExpense.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
                table.append("<tr><th style='padding: 8px; text-align: left;'>Sr.No</th><th style='padding: 8px; text-align: left;'>Name</th><th style='padding: 8px; text-align: left;'>Amount</th><th style='padding: 8px; text-align: left;'>Category</th><th style='padding: 8px; text-align: left;'>Date</th></tr>");
                int i = 1;
                for (ExpenseDTO expense : todaysExpense) {
                    table.append("<tr>");
                    table.append("<td style='padding: 8px; text-align: left;'>").append(i++).append(". ").append("</td>");
                    table.append("<td style='padding: 8px; text-align: left;'>").append(expense.getName()).append("</td>");
                    table.append("<td style='padding: 8px; text-align: left;'>").append(expense.getAmount()).append("</td>");
                    table.append("<td style='padding: 8px; text-align: left;'>").append(expense.getCategoryName()).append("</td>");
                    table.append("<td style='padding: 8px; text-align: left;'>").append(expense.getDate()).append("</td>");
                    table.append("</tr>");
                }
                table.append("</table>");

                String to = profile.getEmail();
                String subject = "Your Daily Expense Summary";
                String body = "Dear " + profile.getName() + ",<br><br>" +
                        "Here is the summary of your expenses for today:<br><br>" +
                        table.toString() +
                        "<br>Thank you for using MoneyWise!<br><br>" +
                        "Best regards,<br>" +
                        "The MoneyWise Team";
                try {
                    emailService.sendEmail(to, subject, body);
                    log.info("Expense summary email sent to: {}", to);
                } catch (Exception e) {
                    log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
                }
                log.info("Job Ended : sendDailyExpenseSummary()");
            }

        }

    }
}