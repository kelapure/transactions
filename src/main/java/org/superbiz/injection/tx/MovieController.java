package org.superbiz.injection.tx;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * See the transaction-rollback example as it does the same thing
 * via UserTransaction and shows more techniques for rollback
 */
@WebServlet("/movies")
public class MovieController extends HttpServlet {

    @EJB
    private Movies movies;

    @EJB
    private Caller transactionalCaller;

    private void doWork() throws ServletException {

        try {
            movies.addMovie(new Movie("1", "Quentin Tarantino", "Reservoir Dogs", 1992));
            movies.addMovie(new Movie("2","Joel Coen", "Fargo", 1996));
            movies.addMovie(new Movie("3","Joel Coen", "The Big Lebowski", 1998));
            List<Movie> list = movies.getMovies();
            for (Movie movie : list) {
                movies.deleteMovie(movie);
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
   }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        testWithTransaction();
        out.println("Hello World ");
        out.close();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    public void testWithTransaction() throws ServletException {
        try {
            transactionalCaller.call(new Callable() {
                public Object call() throws Exception {
                    doWork();
                    return null;
                }
            });
        } catch (Exception e) {
           throw new ServletException(e);
        }
    }

    public void testWithoutTransaction() throws ServletException {
        try {
            doWork();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public static interface Caller {

        public <V> V call(Callable<V> callable) throws Exception;
    }

    /**
     * This little bit of magic allows our test code to execute in
     * the scope of a container controlled transaction.
     */
    @Stateless
    @TransactionAttribute(REQUIRES_NEW)
    public static class TransactionBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }

    }

}
//END SNIPPET: code
