package com.project.MyEplPredictor.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ensures the database schema is compatible with the current entity model.
 * <p>
 * In particular, `Prediction.pointsAwarded` must allow NULL values; older
 * versions of the application created the column NOT NULL, which causes
 * integrity constraint violations when a new prediction is inserted without a
 * score.  Hibernate's `ddl-auto=update` doesn't always change the nullability
 * of an existing column, so we run an explicit ALTER TABLE at startup.
 *
 * This component executes a no-op if the adjustment is unnecessary.
 */
@Component
public class SchemaAdjuster {
    private static final Logger log = LoggerFactory.getLogger(SchemaAdjuster.class);

    private final DataSource dataSource;

    public SchemaAdjuster(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void adjustPredictionTable() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // MySQL syntax; if running against another database this statement
            // may need adjustment. We intentionally ignore errors since the
            // column may already be nullable.
            stmt.execute("ALTER TABLE prediction MODIFY points_awarded INT NULL");
            log.info("Ensured prediction.points_awarded column is nullable");
        } catch (SQLException e) {
            log.debug("Schema adjustment failed or not needed: {}", e.getMessage());
        }
    }
}
