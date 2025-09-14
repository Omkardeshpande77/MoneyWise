package in.omkar.moneywise.service;

import in.omkar.moneywise.dto.ExpenseDTO;
import in.omkar.moneywise.dto.IncomeDTO;
import in.omkar.moneywise.dto.RecentTransactionDTO;
import in.omkar.moneywise.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profileEntity = profileService.getCurrentUser();
        Map<String,Object> returnValue = new LinkedHashMap<>();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> recentTransactions =concat(latestIncomes.stream().map(income-> RecentTransactionDTO.builder()
                .id(income.getId())
                .profileId(profileEntity.getId())
                .icon(income.getIcon())
                .name(income.getName())
                .amount(income.getAmount())
                .date(income.getDate())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .type("Income")
                .build()),
                latestExpenses.stream().map(expense-> RecentTransactionDTO.builder()
                        .id(expense.getId())
                        .profileId(profileEntity.getId())
                        .icon(expense.getIcon())
                        .name(expense.getName())
                        .amount(expense.getAmount())
                        .date(expense.getDate())
                        .createdAt(expense.getCreatedAt())
                        .updatedAt(expense.getUpdatedAt())
                        .type("Expense")
                        .build()))
                .sorted((a,b)->{
                    int cmp = b.getDate().compareTo(a.getDate());
                    if(cmp==0 && a.getCreatedAt()!=null && b.getCreatedAt()!=null){
                        return b.getCreatedAt().compareTo(a.getCreatedAt());

                    }
                    return cmp;
                        }
                        ).toList();

        returnValue.put("totalBalance",
                incomeService.getTotalIncomeForCurrentUser().
                        subtract(expenseService.getTotalExpensesForCurrentUser()));
        returnValue.put("totalIncome",incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense",expenseService.getTotalExpensesForCurrentUser());
        returnValue.put("recent5Expenses",latestExpenses);
        returnValue.put("recent5Incomes",latestIncomes);
        returnValue.put("recentTransactions",recentTransactions);
        return returnValue;
    }
}
