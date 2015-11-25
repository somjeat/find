package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class AbstractDocumentServiceIT<I extends Identifier, E extends Exception> extends AbstractFindIT {
    @Autowired
    protected DocumentsController<I, E> documentsController;

    protected final List<I> indexes;

    public AbstractDocumentServiceIT(final List<I> indexes) {
        this.indexes = new ArrayList<>(indexes);
    }

    @Test
    public void query() throws E {
        final FindQueryParams<I> findQueryParams = createSampleQuery();
        final Documents<FindDocument> documents = documentsController.query(findQueryParams);
        assertThat(documents.getDocuments(), is(not(empty())));
    }

    @Test
    public void queryForPromotions() throws E {
        final FindQueryParams<I> findQueryParams = createSampleQuery();
        final Documents<FindDocument> documents = documentsController.queryForPromotions(findQueryParams);
        assertThat(documents.getDocuments(), is(empty())); // TODO: configure this later
    }

    @Test
    public void findSimilar() throws E {
        final FindQueryParams<I> findQueryParams = createSampleQuery();
        final Documents<FindDocument> documents = documentsController.query(findQueryParams);
        final List<FindDocument> results = documentsController.findSimilar(documents.getDocuments().get(0).getReference(), new HashSet<>(indexes));
        assertThat(results, is(not(empty())));
    }

    private FindQueryParams<I> createSampleQuery() {
        final FindQueryParams<I> findQueryParams = new FindQueryParams<>();
        findQueryParams.setText("*");
        findQueryParams.setMaxResults(50);
        findQueryParams.setIndex(indexes);
        return findQueryParams;
    }
}
