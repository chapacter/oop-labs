package ru.ssau.tk.avokado.lab2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.tk.avokado.lab2.dto.CreatePointRequest;
import ru.ssau.tk.avokado.lab2.dto.PointDto;
import ru.ssau.tk.avokado.lab2.dto.UpdatePointRequest;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.exception.ResourceNotFoundException;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.service.PointService;

@Service
@Transactional
public class PointServiceImpl implements PointService {

    private static final Logger logger = LoggerFactory.getLogger(PointServiceImpl.class);

    private final PointRepository pointRepository;
    private final FunctionRepository functionRepository;

    public PointServiceImpl(PointRepository pointRepository, FunctionRepository functionRepository) {
        this.pointRepository = pointRepository;
        this.functionRepository = functionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointDto> list(Long functionId, Pageable pageable) {
        logger.debug("PointService.list functionId={} pageable={}", functionId, pageable);
        if (functionId == null) {
            return pointRepository.findAll(pageable).map(this::toDto);
        } else {
            FunctionEntity f = functionRepository.findById(functionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Function not found: " + functionId));
            return pointRepository.findByFunction(f, pageable).map(this::toDto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PointDto get(Long id) {
        logger.debug("PointService.get id={}", id);
        TabulatedPoint p = pointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found: " + id));
        return toDto(p);
    }

    @Override
    public PointDto create(CreatePointRequest req) {
        logger.info("PointService.create functionId={} index={}", req.functionId(), req.indexInFunction());
        FunctionEntity f = functionRepository.findById(req.functionId())
                .orElseThrow(() -> new ResourceNotFoundException("Function not found: " + req.functionId()));
        TabulatedPoint p = new TabulatedPoint();
        p.setIndexInFunction(req.indexInFunction());
        p.setX(req.x());
        p.setY(req.y());
        p.setFunction(f);
        TabulatedPoint saved = pointRepository.save(p);
        return toDto(saved);
    }

    @Override
    public PointDto update(Long id, UpdatePointRequest req) {
        logger.info("PointService.update id={} index={} x={} y={}", id, req.indexInFunction(), req.x(), req.y());
        TabulatedPoint p = pointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found: " + id));
        if (req.indexInFunction() != null) p.setIndexInFunction(req.indexInFunction());
        if (req.x() != null) p.setX(req.x());
        if (req.y() != null) p.setY(req.y());
        TabulatedPoint saved = pointRepository.save(p);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        logger.info("PointService.delete id={}", id);
        if (!pointRepository.existsById(id)) {
            throw new ResourceNotFoundException("Point not found: " + id);
        }
        pointRepository.deleteById(id);
    }

    private PointDto toDto(TabulatedPoint p) {
        Long functionId = p.getFunction() != null ? p.getFunction().getId() : null;
        return new PointDto(p.getId(), p.getIndexInFunction(), p.getX(), p.getY(), functionId);
    }
}
