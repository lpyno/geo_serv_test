package cl.redd.objects

case class GeofGetAllPagNewReq ( filter:Option[FilterMain]=None, paginated:Option[Map[String,Int]]=None )
/**
{
        "filter":{
                "realm":"rslite",
                "companyId":144,
                "filter":[
                    {"name":"baquedano"}
                ]
            },
            "paginated":{"limit":10,"offset":0}
        }
*/

