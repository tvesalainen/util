package org.vesalainen.math;

import java.io.Serializable;
import org.vesalainen.math.matrix.DoubleMatrix;

/**
 * <p>
 * This is a straight forward implementation of the Levenberg-Marquardt (LM)
 * algorithm. LM is used to minimize non-linear cost functions:<br>
 * <br>
 * S(P) = Sum{ i=1:m , [y<sub>i</sub> - f(x<sub>i</sub>,P)]<sup>2</sup>}<br>
 * <br>
 * where P is the set of parameters being optimized.
 * </p>
 *
 * <p>
 * In each iteration the parameters are updated using the following
 * equations:<br>
 * <br>
 * P<sub>i+1</sub> = (H + &lambda; I)<sup>-1</sup> d <br>
 * d = (1/N) Sum{ i=1..N , (f(x<sub>i</sub>;P<sub>i</sub>) - y<sub>i</sub>) *
 * jacobian(:,i) } <br>
 * H = (1/N) Sum{ i=1..N , jacobian(:,i) * jacobian(:,i)<sup>T</sup> }
 * </p>
 * <p>
 * Whenever possible the allocation of new memory is avoided. This is
 * accomplished by reshaping matrices. A matrix that is reshaped won't grow
 * unless the new shape requires more memory than it has available.
 * </p>
 *
 * @author Peter Abeles
 */
public class LevenbergMarquardt implements Serializable
{

    private static final long serialVersionUID = 1L;
    // how much the numerical jacobian calculation perturbs the parameters by.
    // In better implementation there are better ways to compute this delta.  See Numerical Recipes.
    private final static double DELTA = 1e-8;

    private int iter1 = 25;
    private int iter2 = 5;
    private double maxDifference = 1e-8;

    private double initialLambda;

    // the function that is optimized
    private Function func;
    private JacobianFactory jacobianFactory;

    // the optimized parameters and associated costs
    private DoubleMatrix param;
    private double initialCost;
    private double finalCost;

    // used by matrix operations
    private DoubleMatrix d;
    private DoubleMatrix H;
    private DoubleMatrix negDelta;
    private DoubleMatrix tempParam;
    private DoubleMatrix A;

    // variables used by the numerical jacobian algorithm
    private DoubleMatrix temp0;
    private DoubleMatrix temp1;
    // used when computing d and H variables
    private DoubleMatrix tempDH;

    // Where the numerical Jacobian is stored.
    private DoubleMatrix jacobian;

    /**
     * Creates a new instance that uses the provided cost function.
     *
     * @param func Cost function that is being optimized.
     */
    public LevenbergMarquardt(Function func)
    {
        this(func, null);
    }

    /**
     * Creates a new instance that uses the provided cost function.
     *
     * @param funcCost Cost function that is being optimized.
     * @param jacobianFactory
     */
    public LevenbergMarquardt(Function funcCost, JacobianFactory jacobianFactory)
    {
        this.initialLambda = 1;

        // declare data to some initial small size. It will grow later on as needed.
        int maxElements = 1;
        int numParam = 1;

        this.temp0 = new DoubleMatrix(maxElements, 1);
        this.temp1 = new DoubleMatrix(maxElements, 1);
        this.tempDH = new DoubleMatrix(maxElements, 1);
        this.jacobian = new DoubleMatrix(numParam, maxElements);

        this.func = funcCost;
        this.jacobianFactory = jacobianFactory;

        this.param = new DoubleMatrix(numParam, 1);
        this.d = new DoubleMatrix(numParam, 1);
        this.H = new DoubleMatrix(numParam, numParam);
        this.negDelta = new DoubleMatrix(numParam, 1);
        this.tempParam = new DoubleMatrix(numParam, 1);
        this.A = new DoubleMatrix(numParam, numParam);
    }

    public double getInitialCost()
    {
        return initialCost;
    }

    public double getFinalCost()
    {
        return finalCost;
    }

    public DoubleMatrix getParameters()
    {
        return param;
    }

    /**
     * Finds the best fit parameters.
     *
     * @param initParam The initial set of parameters for the function.
     * @param X The inputs to the function.
     * @param Y The "observed" output of the function
     * @return true if it succeeded and false if it did not.
     */
    public boolean optimize(DoubleMatrix initParam,
            DoubleMatrix X,
            DoubleMatrix Y)
    {
        if (X.rows() == 0)
        {
            return false;
        }
        configure(initParam, X, Y);

        // save the cost of the initial parameters so that it knows if it improves or not
        initialCost = cost(param, X, Y);

        // iterate until the difference between the costs is insignificant
        // or it iterates too many times
        if (!adjustParam(X, Y, initialCost))
        {
            finalCost = Double.NaN;
            return false;
        }

        return true;
    }

    /**
     * Iterate until the difference between the costs is insignificant or it
     * iterates too many times
     */
    private boolean adjustParam(DoubleMatrix X, DoubleMatrix Y,
            double prevCost)
    {
        // lambda adjusts how big of a step it takes
        double lambda = initialLambda;
        // the difference between the current and previous cost
        double difference = 1000;

        for (int iter = 0; iter < iter1 && difference > maxDifference; iter++)
        {
            // compute some variables based on the gradient
            computeDandH(param, X, Y);

            // try various step sizes and see if any of them improve the
            // results over what has already been done
            boolean foundBetter = false;
            for (int i = 0; i < iter2; i++)
            {
                computeA(A, H, lambda);

                if (!DoubleMatrix.solve(A, d, negDelta))
                {
                    return false;   // throws exception 
                }
                // compute the candidate parameters
                DoubleMatrix.subtract(param, negDelta, tempParam);

                double cost = cost(tempParam, X, Y);
                if (cost < prevCost)
                {
                    // the candidate parameters produced better results so use it
                    foundBetter = true;
                    param.set(tempParam);
                    difference = prevCost - cost;
                    prevCost = cost;
                    lambda /= 10.0;
                }
                else
                {
                    lambda *= 10.0;
                }
            }

            // it reached a point where it can't improve so exit
            if (!foundBetter)
            {
                break;
            }
        }
        finalCost = prevCost;
        return true;
    }

    /**
     * Performs sanity checks on the input data and reshapes internal matrices.
     * By reshaping a matrix it will only declare new memory when needed.
     */
    protected void configure(DoubleMatrix initParam, DoubleMatrix X, DoubleMatrix Y)
    {
        if (Y.rows() != X.rows())
        {
            throw new IllegalArgumentException("Different vector lengths");
        }
        else if (Y.columns() != 1 /*|| X.cols() != 1*/)
        {
            throw new IllegalArgumentException("Inputs must be a column vector");
        }

        int numParam = initParam.elements();
        int numPoints = Y.rows();

        if (param.elements() != initParam.elements())
        {
            // reshaping a matrix means that new memory is only declared when needed
            this.param.reshape(numParam, 1, false);
            this.d.reshape(numParam, 1, false);
            this.H.reshape(numParam, numParam, false);
            this.negDelta.reshape(numParam, 1, false);
            this.tempParam.reshape(numParam, 1, false);
            this.A.reshape(numParam, numParam, false);
        }

        param.set(initParam);

        // reshaping a matrix means that new memory is only declared when needed
        temp0.reshape(numPoints, 1, false);
        temp1.reshape(numPoints, 1, false);
        tempDH.reshape(numPoints, 1, false);
        jacobian.reshape(numParam, numPoints, false);

    }

    /**
     * Computes the d and H parameters. Where d is the average error gradient
     * and H is an approximation of the hessian.
     */
    private void computeDandH(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y)
    {
        func.compute(param, x, tempDH);
        DoubleMatrix.subtractEquals(tempDH, y);

        if (jacobianFactory != null)
        {
            jacobianFactory.computeJacobian(param, x, jacobian);
        }
        else
        {
            computeNumericalJacobian(param, x, jacobian);
        }

        int numParam = param.elements();
        int length = y.elements();

        // d = average{ (f(x_i;p) - y_i) * jacobian(:,i) }
        for (int i = 0; i < numParam; i++)
        {
            double total = 0;
            for (int j = 0; j < length; j++)
            {
                total += tempDH.get(j, 0) * jacobian.get(i, j);
            }
            d.set(i, 0, total / length);
        }

        // compute the approximation of the hessian
        DoubleMatrix.multTransB(jacobian, jacobian, H);
        DoubleMatrix.scale(1.0 / length, H);
    }

    /**
     * A = H + lambda*I <br>
     * <br>
     * where I is an identity matrix.
     */
    private void computeA(DoubleMatrix A, DoubleMatrix H, double lambda)
    {
        final int numParam = param.elements();

        A.set(H);
        for (int i = 0; i < numParam; i++)
        {
            A.set(i, i, A.get(i, i) + lambda);
        }
    }

    /**
     * Computes the "cost" for the parameters given.
     *
     * cost = (1/N) Sum (f(x;p) - y)^2
     */
    public double cost(DoubleMatrix param, DoubleMatrix X, DoubleMatrix Y)
    {
        func.compute(param, X, temp0);

        double error = DoubleMatrix.diffNorm(temp0, Y);

        return error * error / (double) X.rows();
    }

    /**
     * Computes a simple numerical Jacobian.
     *
     * @param param The set of parameters that the Jacobian is to be computed
     * at.
     * @param pt The point around which the Jacobian is to be computed.
     * @param deriv Where the jacobian will be stored
     */
    protected void computeNumericalJacobian(DoubleMatrix param,
            DoubleMatrix pt,
            DoubleMatrix deriv)
    {
        double invDelta = 1.0 / DELTA;

        func.compute(param, pt, temp0);

        // compute the jacobian by perturbing the parameters slightly
        // then seeing how it effects the results.
        for (int i = 0; i < param.rows(); i++)
        {
            param.add(i, 0, DELTA);
            func.compute(param, pt, temp1);
            // compute the difference between the two parameters and divide by the delta
            DoubleMatrix.add(invDelta, temp1, -invDelta, temp0, temp1);
            // copy the results into the jacobian matrix
            for (int r = 0; r < temp1.rows(); r++)
            {
                deriv.set(i, r, temp1.get(r, 0));
            }

            param.sub(i, 0, DELTA);
        }
    }

    /**
     * Sets the main loop count. Initially 25
     *
     * @param iter1
     */
    public void setIter1(int iter1)
    {
        this.iter1 = iter1;
    }

    /**
     * Sets the inner loop count. Initially 5
     *
     * @param iter2
     */
    public void setIter2(int iter2)
    {
        this.iter2 = iter2;
    }

    /**
     * Sets the max cost difference
     *
     * @param maxDifference
     */
    public void setMaxDifference(double maxDifference)
    {
        this.maxDifference = maxDifference;
    }

    /**
     * The function that is being optimized.
     */
    @FunctionalInterface
    public interface Function
    {

        /**
         * Computes the output for each value in matrix x given the set of
         * parameters.
         *
         * @param param The parameter for the function.
         * @param x the input points.
         * @param y the resulting output.
         */
        public void compute(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y);
    }

    /**
     * Jacobian matrix creator
     */
    @FunctionalInterface
    public interface JacobianFactory
    {

        public void computeJacobian(DoubleMatrix param, DoubleMatrix x, DoubleMatrix jacobian);
    }
}
