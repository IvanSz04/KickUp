import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickup.Model.Consejos
import com.example.kickup.R

//ADAPTADOR PARA MOSTRAR UNA LISTA DE CONSEJOS EN UN RECYCLERVIEW
class ConsejoAdapter(

    //LISTA DE OBJETOS CONSEJO Y UNA FUNCIÓN PARA CUANDO SE HAGA CLICK EN UN ITEM (LLAMAMOS A CONSEJOS)
    private var consejos: List<Consejos>,
    private val onItemClick: (Consejos) -> Unit
) : RecyclerView.Adapter<ConsejoAdapter.ConsejoViewHolder>() {

    //CLASE VIEWHOLDER QUE CONTIENE LA LÓGICA PARA CADA ITEM INDIVIDUAL
    inner class ConsejoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //FUNCIÓN PARA LLENAR LOS DATOS DE CADA ITEM DEL RECYCLER
        fun bind(consejo: Consejos) {
            //SE MUESTRA SOLO EL TÍTULO DEL CONSEJO EN EL ITEM
            itemView.findViewById<TextView>(R.id.tvTittle).text = consejo.tittle

            //SE DEFINE LA ACCIÓN AL HACER CLICK EN EL ITEM
            itemView.setOnClickListener {
                onItemClick(consejo)
            }
        }
    }

    //CREA UNA NUEVA VISTA PARA UN ITEM
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsejoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consejo, parent, false) //LLENA EL LAYOUT DEL ITEM CON EL CONSEJO SELECCIONADO
        return ConsejoViewHolder(view)
    }

    //LLAMA A LA FUNCIÓN BIND PARA LLENAR LOS DATOS DEL ITEM EN LA POSICIÓN
    override fun onBindViewHolder(holder: ConsejoViewHolder, position: Int) {
        holder.bind(consejos[position])
    }

    //CANTIDAD TOTAL DE ELEMENTOS EN LA LISTA
    override fun getItemCount(): Int = consejos.size
}