import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Интерфейс стратегии расчёта зарплаты
interface SalaryCalculationStrategy {
    double calculate(double baseAmount);
}

// Стратегия базовой зарплаты
class BasicSalaryStrategy implements SalaryCalculationStrategy {
    @Override
    public double calculate(double baseAmount) {
        return baseAmount;
    }
}

// Стратегия зарплаты с надбавкой
class BonusSalaryStrategy implements SalaryCalculationStrategy {
    private final double bonusPercentage;

    public BonusSalaryStrategy(double bonusPercentage) {
        if (bonusPercentage < 0 || bonusPercentage > 200) {
            throw new IllegalArgumentException("Надбавка должна быть в диапазоне от 0% до 200%");
        }
        this.bonusPercentage = bonusPercentage;
    }

    @Override
    public double calculate(double baseAmount) {
        return baseAmount * (1 + bonusPercentage / 100.0);
    }
}

// Абстрактный класс для типов работ
abstract class Work {
    protected static final double MAX_SALARY = 1_000_000.0;

    protected String name;
    protected double baseAmount;
    protected SalaryCalculationStrategy strategy;
    protected double finalSalary;

    public Work(String name, double baseAmount, SalaryCalculationStrategy strategy) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название работы не может быть пустым");
        }
        if (baseAmount < 0) {
            throw new IllegalArgumentException("Базовая сумма оплаты не может быть отрицательной");
        }

        this.name = name.trim();
        this.baseAmount = baseAmount;
        this.strategy = strategy;

        // Рассчитываем итоговую зарплату ДО сохранения
        this.finalSalary = strategy.calculate(baseAmount);

        // Проверка на максимальную зарплату
        if (this.finalSalary > MAX_SALARY) {
            throw new IllegalArgumentException(
                    String.format("Итоговая зарплата (%.2f руб.) превышает максимальную допустимую сумму (%.2f руб.)",
                            this.finalSalary, MAX_SALARY));
        }
    }

    public double getSalary() {
        return finalSalary; // Возвращаем уже рассчитанное и проверенное значение
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f руб. (база: %.2f руб., стратегия: %s)",
                name, getSalary(), baseAmount, strategy.getClass().getSimpleName());
    }
}

// Тип работы без надбавки
class SimpleWork extends Work {
    public SimpleWork(String name, double baseAmount) {
        super(name, baseAmount, new BasicSalaryStrategy());
    }
}

// Тип работы с надбавкой
class BonusWork extends Work {
    public BonusWork(String name, double baseAmount, double bonusPercentage) {
        super(name, baseAmount, new BonusSalaryStrategy(bonusPercentage));
    }
}

// Класс отдела расчёта зарплаты
class SalaryDepartment {
    private final List<Work> works = new ArrayList<>();

    public void addWork(Work work) {
        works.add(work);
    }

    public double calculateAverageSalary() {
        if (works.isEmpty()) {
            throw new IllegalStateException("Не добавлено ни одного типа работ");
        }

        double total = 0;
        for (Work work : works) {
            total += work.getSalary();
        }
        return total / works.size();
    }

    public void printAllWorks() {
        if (works.isEmpty()) {
            System.out.println("Список работ пуст.");
            return;
        }
        System.out.println("\n=== Список всех типов работ ===");
        for (int i = 0; i < works.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, works.get(i));
        }
    }
}

// Основной класс с интерактивным меню
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final SalaryDepartment department = new SalaryDepartment();

    public static void main(String[] args) {
        System.out.println("Добро пожаловать в систему расчёта зарплат!");
        System.out.println("Максимальная зарплата: 1 000 000 руб.\n");

        while (true) {
            showMenu();
            String choice = readString("Выберите действие: ").trim();

            switch (choice.toLowerCase()) {
                case "1":
                    addNewWork();
                    break;
                case "2":
                    try {
                        double avg = department.calculateAverageSalary();
                        System.out.printf("\n✅ Средняя зарплата по всем видам работ: %.2f руб.\n\n", avg);
                    } catch (IllegalStateException e) {
                        System.err.println("⚠️ " + e.getMessage() + "\n");
                    }
                    break;
                case "3":
                    department.printAllWorks();
                    break;
                case "4":
                    System.out.println("\nДо свидания! Работа системы завершена.");
                    return;
                default:
                    System.err.println("❌ Неверный выбор. Попробуйте снова.\n");
            }
        }
    }

    private static void showMenu() {
        System.out.println("=== МЕНЮ ===");
        System.out.println("1. Добавить новый тип работы");
        System.out.println("2. Рассчитать среднюю зарплату");
        System.out.println("3. Показать все типы работ");
        System.out.println("4. Выход");
        System.out.println("==================");
    }

    private static void addNewWork() {
        try {
            String name = readString("Введите название работы: ");
            double baseAmount = readDouble("Введите базовую сумму оплаты (руб.): ");

            System.out.println("\nВыберите тип оплаты:");
            System.out.println("1. Базовая оплата (без надбавки)");
            System.out.println("2. Оплата с надбавкой (в процентах)");

            int typeChoice = readInt("Ваш выбор (1 или 2): ");

            if (typeChoice == 1) {
                Work work = new SimpleWork(name, baseAmount);
                department.addWork(work);
                System.out.printf("✅ Добавлен тип работы: %s\n\n", work.getName());

            } else if (typeChoice == 2) {
                double bonusPercent = readDouble("Введите процент надбавки (0–200%): ");
                Work work = new BonusWork(name, baseAmount, bonusPercent);
                department.addWork(work);
                System.out.printf("✅ Добавлен тип работы с надбавкой: %s\n\n", work.getName());

            } else {
                System.err.println("❌ Неверный выбор типа оплаты. Отмена операции.\n");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Ошибка: " + e.getMessage() + "\n");
        }
    }

    // Утилитные методы валидации ввода
    private static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input;
            }
            System.err.println("❌ Введите непустое значение.");
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.err.println("❌ Введите корректное число (например: 120000.0).");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.err.println("❌ Введите целое число (1 или 2).");
            }
        }
    }
}