function runCouponsPromise(couponsPromise){
    couponsPromise.then((successMessage) => {
        $('#coupons').DataTable({
            data: JSON.parse(successMessage),
            "columns": [
                {"data": "dateReceived"},
                {
                    "data": "subject",
                    "render": function ( data, type, row, meta ) {
                        return '<a href=' + getApiBaseUrl("/body") + '?bodyId=' + row.id + '>' + row.subject + '</a>';
                    }
                }//,

//                {"data": "comment"},
//                {"data": "expirationDate"},
//                {
//                    "data": null,
//                    "defaultContent": "",
//                    "render": function(data,type,row,meta) {
//                        return (row.dateDeleted ? row.dateDeleted : '<a href=' + getApiBaseUrl("setDateDeleted") + '?id=' + row.id +'>Delete');
//                    }
//                }
            ],
            responsive: true,
            "pageLength": 50
        });
    });
}