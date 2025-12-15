package ru.ssau.tk.avokado.lab2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.tk.avokado.lab2.dto.CreateFunctionRequest;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.SimpleUserDto;
import ru.ssau.tk.avokado.lab2.dto.UpdateFunctionRequest;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.exception.ResourceNotFoundException;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;
import ru.ssau.tk.avokado.lab2.service.FunctionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FunctionServiceImpl implements FunctionService {

    private static final Logger logger = LoggerFactory.getLogger(FunctionServiceImpl.class);

    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    public FunctionServiceImpl(FunctionRepository functionRepository,
                               UserRepository userRepository,
                               PointRepository pointRepository) {
        this.functionRepository = functionRepository;
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FunctionDto> list(String nameFilter, Long userId, boolean withPoints, Pageable pageable) {
        logger.debug("FunctionService.list nameFilter='{}' userId={} withPoints={} pageable={}",
                nameFilter, userId, withPoints, pageable);
        Page<FunctionEntity> p;
        if (userId != null) {
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            p = functionRepository.findByUser(u, pageable);
            return p.map(f -> toDto(f, withPoints));
        } else if (nameFilter == null || nameFilter.isBlank()) {
            p = functionRepository.findAll(pageable);
        } else {
            p = functionRepository.findByNameContaining(nameFilter, pageable);
        }
        return p.map(f -> toDto(f, withPoints));
    }

    @Transactional(readOnly = true)
    public Page<FunctionDto> list(String nameFilter, Long userId, Pageable pageable) {
        return list(nameFilter, userId, false, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FunctionDto get(Long id, boolean withPoints) {
        logger.debug("FunctionService.get id={}, withPoints={}", id, withPoints);
        FunctionEntity f = functionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Function not found: " + id));
        return toDto(f, withPoints);
    }

    @Override
    public FunctionDto create(CreateFunctionRequest req) {
        logger.info("FunctionService.create name={} userId={}", req.name(), req.userId());
        User u = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.userId()));
        FunctionEntity f = new FunctionEntity();
        f.setName(req.name());
        f.setFormat(req.format());
        f.setFuncResult(req.funcResult());
        f.setUser(u);
        FunctionEntity saved = functionRepository.save(f);
        return toDto(saved, false);
    }

    @Override
    public FunctionDto update(Long id, UpdateFunctionRequest req) {
        logger.info("FunctionService.update id={} name={} format={}", id, req.name(), req.format());
        FunctionEntity f = functionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Function not found: " + id));
        if (req.name() != null) f.setName(req.name());
        if (req.format() != null) f.setFormat(req.format());
        if (req.funcResult() != null) f.setFuncResult(req.funcResult());
        FunctionEntity saved = functionRepository.save(f);
        return toDto(saved, false);
    }

    @Override
    public void delete(Long id) {
        logger.info("FunctionService.delete id={}", id);
        if (!functionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Function not found: " + id);
        }
        functionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FunctionDto> findByUserId(Long userId) {
        logger.debug("FunctionService.findByUserId userId={}", userId);
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return functionRepository.findByUser(u).stream().map(f -> toDto(f, false)).collect(Collectors.toList());
    }

    private FunctionDto toDto(FunctionEntity f, boolean withPoints) {
        Long pointsCount = null;
        if (withPoints) {
            pointsCount = pointRepository.countByFunction(f);
        }
        ru.ssau.tk.avokado.lab2.dto.SimpleUserDto su = new SimpleUserDto(
                f.getUser() != null ? f.getUser().getId() : null,
                f.getUser() != null ? f.getUser().getName() : null
        );
        return new FunctionDto(f.getId(), f.getName(), f.getFormat(), f.getFuncResult(), su, pointsCount);
    }
}
