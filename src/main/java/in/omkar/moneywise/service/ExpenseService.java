package in.omkar.moneywise.service;

import in.omkar.moneywise.dto.ExpenseDTO;
import in.omkar.moneywise.entity.CategoryEntity;
import in.omkar.moneywise.entity.ExpenseEntity;
import in.omkar.moneywise.entity.ProfileEntity;
import in.omkar.moneywise.repository.CategoryRepository;
import in.omkar.moneywise.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final ExpenseRepository expenseRepository;

    public ExpenseDTO addExpense(ExpenseDTO dto){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        Optional<CategoryEntity> category=categoryRepository.findById(dto.getCategoryId());
        if (category.isEmpty()){
            throw new RuntimeException("Category not found");
        }
        ExpenseEntity entity= toEntity(dto,profileEntity,category.get());
        entity=expenseRepository.save(entity);
        return toDTO(entity);
    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
       ProfileEntity profileEntity = profileService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profileEntity.getId(), startOfMonth, endOfMonth);
        return expenses.stream().map(this::toDTO).toList();
    }

    public void deleteExpense(Long expenseId) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        ProfileEntity currentUser = profileService.getCurrentUser();
        if (!(expense.getProfile().getId()==currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseDTO>getLatest5ExpensesForCurrentUser(){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        List<ExpenseEntity> expenses=expenseRepository.findTop5ByProfileIdOrderByDateDesc(profileEntity.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpensesForCurrentUser(){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        BigDecimal totalExpense=expenseRepository.findTotalExpenseByProfileId(profileEntity.getId());
        return totalExpense != null ? totalExpense : BigDecimal.ZERO;
    }

    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyWord, Sort sort){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        List<ExpenseEntity> expenseDTOList = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profileEntity.getId(),startDate,endDate,keyWord,sort);
        return expenseDTOList.stream().map(this::toDTO).toList();
    }

    public List<ExpenseDTO> getExpenseForUserOnDate(Long profileId, LocalDate date){
        List<ExpenseEntity> expenseDTOList = expenseRepository.findByProfileIdAndDate(profileId,date);
        return expenseDTOList.stream().map(this::toDTO).toList();
    }

    private ExpenseEntity toEntity(ExpenseDTO dto , ProfileEntity profile, CategoryEntity category){
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }
    private ExpenseDTO toDTO(ExpenseEntity entity){
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .build();
    }
}
