package ru.ssau.tk.avokado.lab2.bench;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Наполняет БД через JPA (saveAll партиями).
 * Использовать для framework side — все вставки идут через репозитории.
 */
@Component
public class DbPopulatorFramework {

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;

    public DbPopulatorFramework(UserRepository userRepository,
                                FunctionRepository functionRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
    }

    public boolean isPopulated(int requiredFunctions) {
        long cnt = functionRepository.count();
        return cnt >= requiredFunctions;
    }

    /**
     * Заполняет таблицу functions партиями. Создаёт seed user.
     */
    @Transactional
    public void populate(int totalFunctions, int batchSize) {
        System.out.println("Clearing (via repos) and populating " + totalFunctions + " functions via JPA...");
        // Очистка (удаляем функции и пользователей простым способом)
        functionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User seed = new User();
        seed.setName("seed_user");
        seed.setAccessLvl(1);
        seed.setPasswordHash("pass");
        seed = userRepository.save(seed);

        List<FunctionEntity> batch = new ArrayList<>(batchSize);
        int created = 0;
        while (created < totalFunctions) {
            batch.clear();
            int chunk = Math.min(batchSize, totalFunctions - created);
            for (int i = 0; i < chunk; i++) {
                FunctionEntity f = new FunctionEntity();
                f.setName("func_" + (created + i));
                f.setUser(seed);
                f.setFuncResult("result_" + (created + i));
                batch.add(f);
            }
            functionRepository.saveAll(batch);
            created += chunk;
            if (created % 1000 == 0) System.out.println("Inserted " + created + " / " + totalFunctions);
        }
        System.out.println("Populate finished. Total functions = " + functionRepository.count());
    }
}
