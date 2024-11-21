package back.whats_your_ETF.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepository {

    private final Map<Long, List<Long>> userPortfolioPreferences = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Double> portfolioRevenueCache = new ConcurrentHashMap<>();

    public SseEmitter save(String id, SseEmitter emitter) {
        emitters.put(id, emitter);
        return emitter;
    }

    public void deleteById(String id) {
        emitters.remove(id);
    }

    public Map<String, SseEmitter> findAllStartWithById(String id) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(id))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<Long> getUserPortfolioPreferences(Long userId) {
        return userPortfolioPreferences.getOrDefault(userId, new ArrayList<>());
    }

    public void saveUserPortfolioPreferences(Long userId, List<Long> portfolioIds) {
        userPortfolioPreferences.put(userId, portfolioIds);
    }

    public Map<Long, List<Long>> getAllUserPortfolioPreferences() {
        return userPortfolioPreferences;
    }

    public boolean isRevenueChanged(Long portfolioId, double newRevenue) {
        return portfolioRevenueCache.getOrDefault(portfolioId, 0.0) != newRevenue;
    }

    public void updateRevenueCache(Long portfolioId, double newRevenue) {
        portfolioRevenueCache.put(portfolioId, newRevenue);
    }
}
