package ru.ssau.tk.avokado.lab2.bench;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class DbPopulatorFramework {

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public DbPopulatorFramework(UserRepository userRepository,
                                FunctionRepository functionRepository,
                                PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    public boolean isPopulated(int requiredFunctions) {
        long cnt = functionRepository.count();
        return cnt >= requiredFunctions;
    }

    @Transactional
    public void populate(int totalFunctions, int pointsPerFunction, int batchSize) {
        System.out.println("Clearing and populating " + totalFunctions + " functions (and points) via JPA...");
        functionRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User seed = new User();
        seed.setName("seed_user");
        seed.setAccessLvl(1);
        seed.setPasswordHash("pass");
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
            if (created % 1000 == 0) System.out.println("Inserted " + created + " / " + totalFunctions);
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

        System.out.println("Populate finished. functions=" + functionRepository.count() + ", points=" + pointRepository.count());
    }
}
