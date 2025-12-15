package ru.ssau.tk.avokado.lab2.bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.auth.Role;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class DbPopulatorFramework {

    private static final Logger logger = LoggerFactory.getLogger(DbPopulatorFramework.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;
    private final PasswordEncoder passwordEncoder;

    public DbPopulatorFramework(UserRepository userRepository,
                                FunctionRepository functionRepository,
                                PointRepository pointRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isPopulated(int requiredFunctions) {
        long cnt = functionRepository.count();
        return cnt >= requiredFunctions;
    }

    @Transactional
    public void populate(int totalFunctions, int pointsPerFunction, int batchSize) {
        logger.info("Starting populate: functions={}, pointsPerFunction={}, batchSize={}", totalFunctions, pointsPerFunction, batchSize);
        // Сначала очищаем точки, затем функции, затем пользователей — чтобы не было нарушений FK.
        pointRepository.deleteAllInBatch();
        functionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User seed = new User();
        seed.setName("seed_user");
        seed.setAccessLvl(1);
        // hash password
        seed.setPasswordHash(passwordEncoder.encode("pass"));
        // give roles
        seed.getRoles().add(Role.ROLE_USER);
        seed.getRoles().add(Role.ROLE_ADMIN);
        seed = userRepository.save(seed);

        List<FunctionEntity> batchF = new ArrayList<>(batchSize);
        int created = 0;
        while (created < totalFunctions) {
            batchF.clear();
            int chunk = Math.min(batchSize, totalFunctions - created);
            for (int i = 0; i < chunk; i++) {
                FunctionEntity f = new FunctionEntity();
                f.setName("func_" + (created + i));
                f.setUser(seed);
                f.setFuncResult("result_" + (created + i));
                batchF.add(f);
            }
            functionRepository.saveAll(batchF);
            created += chunk;
            if (created % 1000 == 0) logger.info("Inserted {} / {}", created, totalFunctions);
        }

        if (pointsPerFunction > 0) {
            List<TabulatedPoint> pBatch = new ArrayList<>(batchSize * 2);
            List<FunctionEntity> allFuncs = functionRepository.findAll();
            for (int i = 0; i < allFuncs.size(); i++) {
                FunctionEntity f = allFuncs.get(i);
                for (int j = 0; j < pointsPerFunction; j++) {
                    TabulatedPoint p = new TabulatedPoint();
                    p.setIndexInFunction(j);
                    p.setX(i + j * 0.1);
                    p.setY(Math.sin(i + j));
                    p.setFunction(f);
                    pBatch.add(p);
                    if (pBatch.size() >= batchSize) {
                        pointRepository.saveAll(pBatch);
                        pBatch.clear();
                    }
                }
            }
            if (!pBatch.isEmpty()) pointRepository.saveAll(pBatch);
        }

        logger.info("Populate finished. functions={}, points={}",
                functionRepository.count(), pointRepository.count());
    }

    @Transactional
    public void clearAll() {
        pointRepository.deleteAllInBatch();
        functionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        logger.info("Cleared all tables via populator");
    }
}
