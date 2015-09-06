
package in.thyferny.nlp.suggest.scorer;


public interface ISentenceKey<T>
{
    Double similarity(T other);
}
