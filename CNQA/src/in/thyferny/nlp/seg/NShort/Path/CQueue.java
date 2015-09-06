
package in.thyferny.nlp.seg.NShort.Path;


public class CQueue
{
    private QueueElement pHead = null;
    private QueueElement pLastAccess = null;

    
    public void enQueue(QueueElement newElement)
    {
        QueueElement pCur = pHead, pPre = null;

        while (pCur != null && pCur.weight < newElement.weight)
        {
            pPre = pCur;
            pCur = pCur.next;
        }

        newElement.next = pCur;

        if (pPre == null)
            pHead = newElement;
        else
            pPre.next = newElement;
    }

    
    public QueueElement deQueue()
    {
        if (pHead == null)
            return null;

        QueueElement pRet = pHead;
        pHead = pHead.next;

        return pRet;
    }

    
    public QueueElement GetFirst()
    {
        pLastAccess = pHead;
        return pLastAccess;
    }

    
    public QueueElement GetNext()
    {
        if (pLastAccess != null)
            pLastAccess = pLastAccess.next;

        return pLastAccess;
    }

    
    public boolean CanGetNext()
    {
        return (pLastAccess.next != null);
    }

    
    public void clear()
    {
        pHead = null;
        pLastAccess = null;
    }
}
