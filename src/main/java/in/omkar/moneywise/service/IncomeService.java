package in.omkar.moneywise.service;


import in.omkar.moneywise.dto.ExpenseDTO;
import in.omkar.moneywise.dto.IncomeDTO;
import in.omkar.moneywise.entity.CategoryEntity;
import in.omkar.moneywise.entity.ExpenseEntity;
import in.omkar.moneywise.entity.IncomeEntity;
import in.omkar.moneywise.entity.ProfileEntity;
import in.omkar.moneywise.repository.CategoryRepository;
import in.omkar.moneywise.repository.IncomeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO dto) {

        ProfileEntity profile = profileService.getCurrentUser();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        IncomeEntity entity = toEntity(dto, profile, category);
        entity = incomeRepository.save(entity);
        return toDTO(entity);
    }

    public List<IncomeDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profileEntity = profileService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> expenses = incomeRepository.findByProfileIdAndDateBetween(profileEntity.getId(), startOfMonth, endOfMonth);
        return expenses.stream().map(this::toDTO).toList();
    }

    public void deleteIncome(Long expenseId) {
        IncomeEntity income = incomeRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        ProfileEntity currentUser = profileService.getCurrentUser();
        if (!(income.getProfile().getId()==currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this expense");
        }
        incomeRepository.delete(income);
    }

    public List<IncomeDTO>getLatest5IncomesForCurrentUser(){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        List<IncomeEntity> expenses=incomeRepository.findTop5ByProfileIdOrderByDateDesc(profileEntity.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyWord, Sort sort){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        List<IncomeEntity> incomeEntities = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profileEntity.getId(),startDate,endDate,keyWord,sort);
        return incomeEntities.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalIncomeForCurrentUser(){
        ProfileEntity profileEntity = profileService.getCurrentUser();
        BigDecimal totalExpense=incomeRepository.findTotalExpenseByProfileId(profileEntity.getId());
        return totalExpense != null ? totalExpense : BigDecimal.ZERO;
    }

    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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
